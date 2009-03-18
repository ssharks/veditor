--------------------------------------------------------------------------------
-- Company: 		 Team Avago
-- Engineer:		 Sebastien Vang
--
-- Create Date:    20:11:01 02/18/06
-- Design Name:    SRAM CLK
-- Module Name:    sramclk - Behavioral
-- Project Name:   Ram Test
-- Target Device:  Spartan 3
-- Tool versions:  7.1
-- Description:	 SRAM/Counter/Clk12.5 Mhz
--
-- Dependencies:	 None
-- 
-- Revision:		 1
-- Revision 0.01 - File Created
-- Additional Comments: None
-- 
--------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

use ieee.numeric_std.ALL;               -- to_unsigned, etc

library work;                           -- for pins entity



-- synopsys translate_off
library UNISIM;
use UNISIM.vcomponents.all;
-- synopsys translate_on

entity main is

   generic (
      ADDR_BITS         :     integer := 19;
      DATA_BITS         :     integer := 32;
      
      LED_COUNT         :     integer := 8;

      LOOP_STEPS        :     integer := 8;

      SLOW_CLOCK_PERIOD :     integer := 50_000_000
      );
   
   port (

      clk               : in  std_logic;

      led_cs_l          : out std_logic;
      led               : out std_logic_vector (0 to LED_COUNT-1);

      reset             : in  std_logic;
      
      -- SRAM ports
      sram_cs_l         : out std_logic;
      -- byte select, for each of the 4 bytes
      sram_bs_l         : out std_logic_vector(0 to 3);
      sram_we_l         : out std_logic;
      sram_oe_l         : out std_logic;

      sram_a            : out std_logic_vector(0 to ADDR_BITS-1);

      sram_d_read       : in  std_logic_vector(0 to DATA_BITS-1);
      sram_d_write      : out std_logic_vector(0 to DATA_BITS-1)
      );
   
end main;



architecture Behavioral of main is

   -- internal register for the read-in RAM value
   signal ram_reg : std_logic_vector(0 to DATA_BITS-1);
   
   -- state
   type state_job_t is (reading, writing);
   type state_t is record
                    job         : state_job_t;
                    i           : integer range 0 to LOOP_STEPS-1;
                    mem_op_step : integer range 0 to SLOW_CLOCK_PERIOD;
                 end record;

   signal state : state_t := (writing, 0, 0);


   -- 1Hz or so clock
   signal clk_slow : std_logic := '0';
   

begin

   -- just display the RAM register on the LEDs
   -- NOTE: the LS-byte is the high-numbered byte.
   led <= ram_reg (DATA_BITS-LED_COUNT to DATA_BITS-1);

   
   -- always writing whole words for now.
   sram_bs_l <= "0000";

   
   -- the big bad monolithic process!
   ram_rw_loop: process (clk, reset)
   begin
      if reset = '1' then

         ram_reg   <= X"AAAAAAAA";
         
         state     <= (writing, 0, 0);

         sram_oe_l <= '1';
         sram_cs_l <= '1';
         sram_we_l <= '1';

         led_cs_l  <= '1';

      elsif rising_edge (clk) then

         if state.job = writing then
            
            sram_oe_l <= '1';  -- no OE
            sram_cs_l <= '0';

            -- WE controlled write to memory
            -- should take 3 cycles.
            case state.mem_op_step is

               when 0 =>
                  -- set address and data
                  sram_a    <= std_logic_vector (to_unsigned
                                                 (8 * state.i,
                                                  ADDR_BITS));
                  -- Twc = 12ns - write cycle - what is this exactly??
                  -- Tsa = 0; no need to wait until asserting WE (write-start)
                  -- do not need to hold after WE goes off (write-end)
                  
                  -- Tsd = 6ns (after both WE and D are asserted)
                  -- Thd = 0 (ie. can remove D at write-end)
                  driver_write_sram:
                     sram_d_write <= std_logic_vector (to_unsigned
                                                       (state.i, DATA_BITS));

                  -- write-start
                  sram_we_l <= '0';

                  state.mem_op_step <= state.mem_op_step + 1;

               when 2 =>
                  -- write-end
                  sram_we_l <= '1';

                  -- on to the next i
                  if state.i = LOOP_STEPS-1 then
                     state.job <= reading;
                     state.i   <= 0;
                  else
                     state.i   <= state.i + 1;
                  end if;

                  state.mem_op_step <= 0;
                  
               when others =>
                  state.mem_op_step <= state.mem_op_step + 1;

            end case;


         else  -- job = reading

            -- address controlled memory read (cycle 1 on data sheet), followed
            -- by display on the LED

            sram_we_l <= '1';
            sram_oe_l <= '0';
            
            -- make sure we're not driving the data bus! need to read from it.
            sram_d_write <= (others => 'Z');

            case state.mem_op_step is
               when 0 =>
                  sram_a    <=  std_logic_vector (to_unsigned
                                                  (8 * state.i,
                                                   ADDR_BITS));
                  -- and now have Taa = 12ns (2 10ns cycles) till data valid;
                  
                  -- begin the read
                  sram_cs_l <= '0';

                  state.mem_op_step <= state.mem_op_step + 1;

               when 3 =>
                  -- read should be done, and the data available.
                  ram_reg <= sram_d_read;

                  -- have Thzce = 6ns after CE goes off until DATA is Z, which
                  -- should be plenty to load the value into ram_reg??
                  -- BUT,
                  -- when SRAM_CS goes off, the bridge immediately cuts off the
                  -- data connection, so the register may not get a chance to load.
                  -- thus, wait one cycle before ending the mem read and
                  -- starting the LED.
                  
                  state.mem_op_step <= state.mem_op_step + 1;

               when 4 =>
                  -- SRAM off, LED on
                  sram_cs_l <= '1';
--                  sram_a    <= (others => 'Z');
                  
                  led_cs_l  <= '0';
                  
                  state.mem_op_step <= state.mem_op_step + 1;

               when SLOW_CLOCK_PERIOD =>
                  -- done with this read and display, on to next
                  led_cs_l          <= '1';

                  if state.i = LOOP_STEPS-1 then
                     -- back to writing
                     state.job <= writing;
                     state.i   <= 0;
                  else
                     state.i   <= state.i + 1;
                  end if;

                  state.mem_op_step <= 0;

               when others =>
                  -- just count
                  state.mem_op_step <= state.mem_op_step + 1;

            end case;

         end if;                        -- job = reading

      end if;                           -- rising_edge(clK)
      
   end process ram_rw_loop;


   


   -- generate a one second or so clock from the main clock
--    one_second_process: process (reset, CLK)
--       variable	counter : integer range 0 to SLOW_CLOCK_PERIOD;
--    begin
--       if reset = '1' then

--          counter := 0;
--          clk_slow <= '0';
         
--       elsif rising_edge (CLK) then

--          counter := counter + 1;
		 
--          if counter >= SLOW_CLOCK_PERIOD then
--             clk_slow <= NOT clk_slow;
-- 			counter := 0;
--          end if;

--       end if;
--    end process one_second_process;


end Behavioral;
