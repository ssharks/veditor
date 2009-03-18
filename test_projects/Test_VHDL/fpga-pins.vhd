library IEEE;
use IEEE.STD_LOGIC_1164.ALL;

entity pins is
   
   -- these are the names used by Avnet for their ucf file, so this is just an
   -- entity to allow mapping my names to their names and using their ucf file
   -- as closely as possible.
   
   port (
      -- instance GLOBAL
      OPB_Clk   : in     std_logic;
      
      A         : out    std_logic_vector(0 to 31);  -- A, OE, WE, D are shared flash & sram
      OE_L      : out    std_logic;
      RST_L     : out    std_logic;
      WE_L      : buffer std_logic;
      D         : inout  std_logic_vector(0 to 31);
--	FLASH_CS_L    : buffer std_logic_vector(0 to 0);
--	
      SRAM_CS_L : inout  std_logic_vector(0 to 0); -- set up as vector to match output of platgen
      SRAM_BS_L : out    std_logic_vector(0 to 3)   -- "bs" signals control "BLE#" and "BHE#"
      );	                                                -- on the sram
      
end pins;


architecture dummy of pins is

begin  -- dummy
end dummy;
