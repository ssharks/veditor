-- TestBench Template 
-- edited by sasho

LIBRARY ieee;
USE ieee.std_logic_1164.ALL;
USE ieee.numeric_std.ALL;


ENTITY testbench IS
END testbench;

ARCHITECTURE behavior OF testbench IS
   constant c_offset    : natural := 0; 

   signal   clock            : std_logic := '0';
   signal reset : std_logic := '1';
   
   signal sram_cs_l, we_l, oe_l : std_logic;

   signal D, A : std_logic_vector(0 to 31) := (c_offset-1 => '1', others => '0');
   signal sram_a : std_logic_vector(0 to 18);
   
begin

   -- instantiate the SRAM
   sram: entity async (Behavioral)
      port map (
         CE_b => sram_cs_l,
         WE_b => we_l,
         OE_b => oe_l,
         A    => sram_a,
         DQ   => D);

   
   -- instantiation of the FPGA design
   board : entity work.top (RTL)
      generic map (
         SLOW_CLOCK_PERIOD => 25
         )
      port map (
         CLK_100MHZ       => clock, 
         SYS_RESET_N      => reset,

         D => D,
         A => A,

         SRAM_CS_L => sram_cs_l,

         WE_L => we_l,
         OE_L => oe_l
         );
      
   sram_a <= A(13 to 31);

   clock <= not clock after 5ns;


   -- this is just for simulation, "wait for" will not synthesize.
   reset_control: process
      begin
         wait for 20ns;
         reset <= '0';
         wait for 40ns;
         reset <= '1';

         wait;
      end process;
      
END;
