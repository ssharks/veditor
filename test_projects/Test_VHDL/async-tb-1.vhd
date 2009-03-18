-- TestBench Template 
-- edited by sasho

  LIBRARY ieee;
  USE ieee.std_logic_1164.ALL;
  USE ieee.numeric_std.ALL;
  
  library work;
  
  use work.async;
  use work.package_timing.all;


  ENTITY testbench IS
  END testbench;

ARCHITECTURE behavior OF testbench IS 

	constant addr_len : integer := 19;
	constant data_len : integer := 32;

	signal data  : Std_Logic_Vector(data_len-1 downto 0);
	signal addr : Std_Logic_Vector(addr_len-1 downto 0);
	
	signal oe_l, we_l : std_logic := '1';
	

BEGIN

  -- Component Instantiation
          uut: entity work.async(Behavioral)
				generic map (addr_bits => addr_len,
                             data_bits => data_len)
				port map (
                  CE_b  => '0',
                  WE_b  => we_l,
                  OE_b  => oe_l,
                  A     => addr,
                  DQ    => data
				);


  --  Test Bench Statements
	tb : PROCESS

		variable read_data  : Std_Logic_Vector(data_len-1 downto 0);

	BEGIN
	


        wait for 100 ns; -- wait until global set/reset completes
		  
		  -- Do two writes, wait for the write cycle time, then read it back.
          -- Following write cycle 3 (with on page 7 of datasheet here.
        addr  	<= X"124";
        wait for Tsa;
        we_l 	<= '0';
        data  	<= std_logic_vector (to_unsigned (3456, data_len));
        wait for Tpwe + Thd;
		  
        addr	<= std_logic_vector (to_unsigned (125, addr_len));
        data	<= std_logic_vector (to_unsigned (7890, data_len));
        we_l		<= '0';
        wait for Twc + 20ns;
		  
        we_l      <= '1';	-- needed to read

        wait for 20ns;
        
        -- read address 124 back in
        addr      <= std_logic_vector (to_unsigned (124, addr_len));
        
        wait for Trc + 20ns;	-- read cycle time.
        
        oe_l      <= '0';	-- do the read

        read_data	:= data;	-- copy the value (??)
		  
--		  oe_l		<= '1';		-- done with read
		  
        wait; -- will wait forever
     END PROCESS tb;
  --  End Test Bench 

  END;
