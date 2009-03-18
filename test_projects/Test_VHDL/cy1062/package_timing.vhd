--************************************************************************
--**    MODEL   :       package_timing.vhd                              **
--**    COMPANY :       Cypress Semiconductor                           **
--**    REVISION:       1.0 Created new timing package model            ** 
--************************************************************************


library IEEE,std;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_arith.all;
use IEEE.std_logic_unsigned.all;
use ieee.std_logic_textio.all ;
use std.textio.all ;

--****************************************************************

package package_timing is

------------------------------------------------------------------------------------------------
-- Read Cycle timing
------------------------------------------------------------------------------------------------
constant        Trc    :   TIME    :=   10 ns;     --   Read Cycle Time	    
constant        Taa  		:   TIME    :=   10 ns;     --   Random access AVD Low to Data Valid
constant        Toha  	:   TIME    :=   3 ns;     --   Random access Address to Data Valid
constant        Tace  	:   TIME    :=   10 ns;     --   Random access CS Low to Data Valid
constant  	     Tdoe  	:   TIME    :=   5 ns;     --   Random access AVD High to Data Valid
constant        Tlzoe  :   TIME    :=   1 ns;     --   AVD Low Pulse
constant        Thzoe  :   TIME    :=   5 ns;     --   Address Set-up to AVD rising edge
constant        Tlzce  :   TIME    :=   3 ns;      --   Address Hold from AVD rising edge
constant        Thzce  :   TIME    :=   5 ns;     --   OE Low to Data Valid

------------------------------------------------------------------------------------------------
-- Write Cycle timing
------------------------------------------------------------------------------------------------
constant	       Twc    :   TIME    :=   10 ns;     --   Write Cycle Time
constant        Tsce   :   TIME    :=   7 ns;     --   CS Low to Write End
constant        Taw   	:   TIME    :=   7 ns;     --   Chip select setup to AVD rising edge
constant        Tha   	:   TIME    :=   0 ns;     --   WE Pulse Width
constant        Tsa    :   TIME    :=   0 ns;     --   BLE/BHE Low to Write End
constant        Tpwe   :   TIME    :=   7 ns;     --   Data Set-up to Write End
constant        Tsd    :   TIME    :=   5.5 ns;      --   Data Hold from Write End
constant        Thd  		:   TIME    :=   0 ns;     --   WE Low to High Z
constant        Tlzwe  :   TIME    :=   3 ns;     --   WE High to Low Z
constant        Thzwe  :   TIME    :=   5 ns;     --   AVD High to WE Low


end package_timing;


package body package_timing is

end package_timing;

