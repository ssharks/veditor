library IEEE;
use IEEE.STD_LOGIC_1164.ALL;

library work;                           -- for main entity

-- synthesis translate_off
library UNISIM;
use UNISIM.vcomponents.all;
-- synthesis translate_on

entity top is
   
   -- these are the names used by Avnet for their ucf file, so this is just an
   -- entity to allow mapping my names to their names and using their ucf file
   -- as closely as possible.
   
   generic (
      SLOW_CLOCK_PERIOD : integer := 50_000_000
   );
   
   port (
      CLK_100MHZ   : in    std_logic;
      
      -- this is the pin marked CLK in the schematic; it connects to the
      -- TRGT_CLK on the bridge FPGA.
      CLK_OUT      : out   std_logic;

      -- comes from the reset button, SW1, via the bridge; active-low
      SYS_RESET_N  : in    std_logic;
      
      -- A, OE, WE, D are shared flash & sram      
      -- sasho: both A and D are written as (0 to 31), not sure why, but it
      -- looks like the pin connections in the UCF file depend on this.
      A            : out   std_logic_vector(0 to 31);
      D            : inout std_logic_vector(0 to 31);

      OE_L         : out   std_logic;
      
      SDRAM_CE_L   : out   std_logic;

      WE_L         : out   std_logic;


      FLASH_CS_L   : out   std_logic_vector(0 to 0);
      SYS_ACE_CS_N : out   std_logic;

      GPIO_CS_N    : out   std_logic;
      LED_CS_L     : out   std_logic;

      SRAM_CS_L    : out   std_logic_vector(0 to 0);
      SRAM_BS_L    : out   std_logic_vector(0 to 3)   -- SRAM byte select

      );
   
end top;


architecture RTL of top is
   
   -- to carry an active-high reset signal
   signal my_reset                  : std_logic;

   signal led_i                     : std_logic_vector(0 to 7);
   signal sram_d_read, sram_d_write : std_logic_vector(0 to 31);

   signal sram_a : std_logic_vector (0 to 18);

   signal led_cs_li, sram_cs_li, we_li     : std_logic;

begin
   


   my_reset <= not SYS_RESET_N; -- the reset on pin AH8 is active low, so make
                                -- the internal signal active-high

   main_entity: entity work.main (Behavioral)
       generic map (
          SLOW_CLOCK_PERIOD => SLOW_CLOCK_PERIOD) --make a 1Hz clock
       port map (
          CLK          => CLK_100MHZ,

          LED          => led_i,
          LED_CS_L     => led_cs_li,
          
          RESET        => my_reset,

          sram_cs_l    => sram_cs_li,
          sram_bs_l    => SRAM_BS_L,
          sram_we_l    => we_li,
          sram_oe_l    => OE_L,

          sram_a       => sram_a,
          sram_d_read  => sram_d_read,
          sram_d_write => sram_d_write
          
          );
   

   A <= "0000000000000" & sram_a;
   
   -- only drive D when we really need to write to something.
   data_led  : D <= X"000000" & led_i when led_cs_li = '0'
                    else (others => 'Z');
   data_sram : D <= sram_d_write      when sram_cs_li = '0' and we_li = '0'
                    else (others => 'Z');

--    D <= X"000000" & "01010101" when led_cs_li = '0'                  else
--         X"000000" & "11110000" when sram_cs_li = '0' and we_li = '0' else
--         (others => 'Z');
   
   -- otherwise read D
   sram_d_read  <= D;

   LED_CS_L     <= led_cs_li;
   SRAM_CS_L(0) <= sram_cs_li;
   WE_L         <= we_li;
      
   -- pass on our clock to the bridge FPGA
   CLK_OUT      <= CLK_100MHZ;


   SDRAM_CE_L   <= '1';
   FLASH_CS_L   <= "1";
   
   
   SYS_ACE_CS_N <= '1';
   GPIO_CS_N    <= '1';

end RTL;
