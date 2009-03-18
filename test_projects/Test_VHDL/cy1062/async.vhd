--********************************************************************
--********************************************************************
--** This model is the property of Cypress Semiconductor Corp. and  **
--** is protected by the US copyright laws, any unauthorized copying** 
--** and distribution is prohibited. Cypress reserves the right to  **
--** change any of the functional specifications without any prior  **
--** notice. Cypress is not liable for any damages which may result **
--** from the use of this functional model.                         ** 
--**                                                                **
--** File Name: CY7C1062.vhd                                        **
--**                                                                **
--** Revision : 1.0 - New model created                             **
--** Model    : 16Mb(512Kx32) Fast ASync SRAM                         **   
--**                                                                **
--** Queries  : MPD Applications                                    **
--**            Ph#: (408)-943-2821                                 **
--**            Website: www.cypress.com/support                    **
--********************************************************************
--******************************************************************** 

Library IEEE,work;
Use IEEE.Std_Logic_1164.All;
use IEEE.Std_Logic_Signed.All; 
use work.package_timing.all;
use work.package_utility.all;

-- sasho:
 USE ieee.numeric_std.ALL;

------------------------------------------------------------------------------------------------
-- Entity Description for Async_model
------------------------------------------------------------------------------------------------

Entity async is
generic(ADDR_BITS 					: integer := 19;
	DATA_BITS 						: integer := 32;
	TimingChecks             			: std_logic := '0';
	depth 						: integer := 524288);
Port ( CE_b  					: IN Std_Logic; 		-- Chip Select
       WE_b  					: IN Std_Logic; 		-- Write Enable
       OE_b  					: IN Std_Logic;   -- Output Enable
       A 	 				: IN Std_Logic_Vector(addr_bits-1 downto 0);    -- Address Inputs
       DQ 					: INOUT Std_Logic_Vector(data_bits-1 downto 0):=(others=>'Z'));  -- Read/Write Data
End async;

------------------------------------------------------------------------------------------------
-- End Entity Description
------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------
-- Architecture Description of entity async
------------------------------------------------------------------------------------------------

Architecture Behavioral Of async Is

------------------------------------------------------------------------------------------------
-- Array definition : 512K address space addressed with 32 I/Os
------------------------------------------------------------------------------------------------

Type array1 Is array (depth-1 downto 0) of std_logic_vector(data_bits-1 downto 0);

------------------------------------------------------------------------------------------------
-- Signal Definitions
------------------------------------------------------------------------------------------------

Signal ce,oe,we : Std_logic;
Signal access_wr,access_rd,access_wr_end,wr_end,data_end,oe_end,ce_end,address_end,we_end,rd_end,rd_end1 : Std_logic := '0';
Signal data_out : Std_logic;
Signal address : Std_Logic_vector(addr_bits-1 downto 0);
Signal data : Std_logic_vector(data_bits-1 downto 0);
Signal final_address : Std_logic_vector(addr_bits-1 downto 0);


------------------------------------------------------------------------------------------------
-- Begin Architecture 
------------------------------------------------------------------------------------------------

Begin

	ce <=  CE_b after 0.2 ns;
	oe <=  OE_b;
	we <=  WE_b after 0.2 ns;
	address(addr_bits-1 downto 0) <= A(addr_bits-1 downto 0) after 0.1 ns;
	data(data_bits-1 downto 0) <= DQ(data_bits-1 downto 0) after 0.4 ns;


------------------------------------------------------------------------------------------------
-- Process for signal assignments for Regular Access R/W
------------------------------------------------------------------------------------------------
	PROCESS(ce_b,we_b)
	begin
		if ( ce_b = '0') then 
			access_wr <= (NOT WE_b) after 0.2 ns;
			access_rd <= (WE_b) after 0.2 ns;
		end if;
	end process;
           

------------------------------------------------------------------------------------------------
-- Process for signal transition definitions
------------------------------------------------------------------------------------------------

	PROCESS (ce,we,oe,address,data)

------------------------------------------------------------------------------------------------
--- Assign signals and variables for time checks before reading or writing.
------------------------------------------------------------------------------------------------

	BEGIN
	if ((we'event and we'last_value = '1') or (ce'event and ce'last_value = '1')) then
		wr_end <= '1';
   	elsif ((we'event and we'last_value = '0') or (ce'event and ce'last_value = '0')) then 
    		wr_end <= '0';
   	end if;
	if (data(data_bits-1 downto 0)'event) then
    		data_end <= '1';
   	else 
    		data_end <= '0';
   	end if;
	if (address(addr_bits-1 downto 0)'event) then
    		address_end <= '1';
   	else 
    		address_end <= '0';
   	end if;
   	if (oe'event and oe'last_value = '1') then
    		oe_end <= '1';
   	elsif (oe'event and oe'last_value = '0') then
    		oe_end <= '0';
   	end if;
	if (ce'event and ce'last_value = '1') then
    		ce_end <= '1';
   	elsif (ce'event and ce'last_value = '0') then 
    		ce_end <= '0';
   	end if;
	if (we'event and we'last_value = '1') then
    		we_end <= '1';
   	elsif (we'event and we'last_value = '0') then 
    		we_end <= '0';
   	end if;
	if (access_wr'event and access_wr'last_value = '1') then
    		access_wr_end <= '1';
   	elsif (access_wr'event and access_wr'last_value = '0') then 
    		access_wr_end <= '0';
   	end if;
	if (ce = '0' and we ='0') then
	    	rd_end1 <= '1' after 0.2 ns;
	elsif (ce = '1' or  we ='1') then	
		rd_end1 <= '0' after 0.2 ns;
	end if;
 	if ((oe'event and oe'last_value = '1') or (ce'event and ce'last_value = '1')) then
    		rd_end <= '1';
   	elsif ((oe'event and oe'last_value = '0') or (ce'event and ce'last_value = '0')) then
    		rd_end <= '0';
   	end if;
	END Process;

------------------------------------------------------------------------------------------------
--Process Description for signal to get the read data out for a regular read access
------------------------------------------------------------------------------------------------

	PROCESS(oe,ce,address,A,ce_end,oe_end)
	begin
		if (A'event or oe'event or ce'event) then
			data_out <= '0';
		end if;
		if (access_rd'event and access_rd'last_value ='0' and data_out = '0') then
                  report "level 1 in setting data_out, part 1; oe=" &  std_logic'image(oe)
                     & "address'last_event=" & time'image(address'last_event);
			if (ce = '0') then
				if (oe='0') then
                                  if (address'last_event > (Taa-Tace)) then
                                    data_out <= '1' after Taa;
                                  end if;
				end if;
			end if;
		elsif (access_rd ='1' and data_out ='0') then
                  report
                    "level 1 in setting data_out in part 2; ce=" & std_logic'image(ce)
                    & ",oe=" & std_logic'image(oe);
			if (ce ='0' and oe ='0') then
			        if (address'last_event > (Taa-Tace)) then
					data_out <= '1' after Taa;
				elsif (ce'last_event >= address'last_event) then
					data_out <= '1' after Taa;
				elsif (ce'last_event < address'last_event) then
					data_out <='1' after Tace;
				elsif ((ce'last_event  >= (Tace-Tdoe)) or (address'last_event >= (Taa-Tdoe))) then
					data_out <= '1' after Tdoe;
				end if;
			end if;
		end if;
	end process;

------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------
-- Process Description for the Regular Access write and read cycle 	
------------------------------------------------------------------------------------------------

	PROCESS (ce,we,oe,access_wr,access_rd,address,data,wr_end,rd_end,data_end,address_end,access_wr_end,we_end,ce_end,oe_end,data_out)

	VARIABLE mem_array0 : array1;
	VARIABLE tsd_ok1,tsce_ok1,tpwe_ok1,thd_ok1,tces_ok,tsa_ok,tha_ok :  Std_logic ;
	
	variable data_int, addr_int : integer;
	
   	
	begin

------------------------------------------------------------------------------------------------
------ Perform timing check for Regular Access Read/write.
------------------------------------------------------------------------------------------------

	-- If generic parameter 'TimingChecks' = 1, perform the following checks on timing violations
	if (TimingChecks = '1') then 
		
   		if (access_wr = '1' and (wr_end'event and wr_end = '0')) then
			if (data'last_Event >= Tsd) THEN          
            				tsd_ok1 := '1'; 
            			elsif (data'last_Event < Tsd) THEN          
                 				tsd_ok1 := '0';
            				ASSERT false REPORT "Data setup violation during Access Write"
            				SEVERITY error;
           			end if;
		end if;
		if (access_wr = '1' and (wr_end'event and wr_end = '0')) then
			if (we = '0' and we'last_Event >= Tpwe) THEN          
            				tpwe_ok1 := '1'; 
            			elsif (we = '0' and we'last_Event < Tpwe) THEN          
                  				tpwe_ok1 := '0';
              				ASSERT (tpwe_ok1 = '1') REPORT "Write Enable Pulse Width violation during Access Write"
              				SEVERITY error;
            			end if;
   		end if;
		if (access_wr = '1' and (wr_end'event and wr_end = '0')) then
			if (ce = '0' and ce'last_Event >= Tsce) THEN          
            				tsce_ok1 := '1'; 
            			elsif (ce = '0' and ce'last_Event < Tsce) THEN      
                      				tsce_ok1 := '0';
              				ASSERT (tsce_ok1 = '0') REPORT "Chip Select setup violation during Access Write"
              				SEVERITY error;
            			end if;
   		end if;
		if (access_wr = '1' and wr_end = '0') then
			if (data_end = '1') THEN
				if (wr_end'last_event >= Thd) then
					thd_ok1 := '1'; 
            				else
                 		 			thd_ok1 := '0';
              					ASSERT false REPORT "Data Hold time violation during Access Write"
              					SEVERITY error;
            				end if;
			end if;
   	 	 end if;
		if ((access_wr = '1' or access_rd ='1') and data_end = '1' and address_end = '1') THEN
			if ((data_end'last_event >= Tsa) and (address_end'last_event >= Tsa)) then
				tsa_ok := '1'; 
            			else
               		   		tsa_ok := '0';
              				ASSERT false REPORT "Address Setup time violation during Access Read/Write"
              				SEVERITY error;
            			end if;
	      	end if;
		if (access_wr = '1' or access_rd ='1') then
			if (data_end = '1' and address_end = '1') THEN
				tha_ok := '1'; 
       	     		else
        		          		tha_ok := '0';
              				ASSERT false REPORT "Address Hold time violation during Access Read/Write"
              				SEVERITY error;
            			end if;
		end if;
     	end if;


	data_int := to_integer(unsigned(data));
	addr_int := to_integer(unsigned(address));

------------------------------------------------------------------------------------------------
----- REGULAR ACCESS WRITE CYCLE
------------------------------------------------------------------------------------------------

	if (access_wr = '1' and we = '0' and rd_end1 = '1') then
		if (ce = '0') then
			report "Writing address " & (integer'image(addr_int))
                           & ",data " & (integer'image(data_int));
			mem_array0(conv_integer1(address)) := data(data_bits-1 downto 0);
		end if;
	end if;

-------------------------------------------------------------------------------------------------
----- REGULAR ACCESS READ CYCLE.
------------------------------------------------------------------------------------------------
	
	if (access_rd ='1' and  data_out ='1' and rd_end = '1') then
		report "Reading address " & integer'image(addr_int);
		DQ(data_bits-1 downto 0) <= mem_array0(conv_integer1(address));
	elsif (rd_end ='0') then
		
		report ("reading addr " & integer'image(addr_int) & " but rd_end = 0");
		-- sasho: changed the array literal, which was just 8 bits.
		DQ(data_bits-1 downto 0) <= (others => 'Z') after Thzce;
	end if;
	
	End Process;

------------------------------------------------------------------------------------------------

End Behavioral;
------------------------------------------------------------------------------------------------
-- End Architecture Description
------------------------------------------------------------------------------------------------
