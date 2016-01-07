-- =============================================================================
-- file name is:  mux4_1.vhd       (mux=multiplexer)
-- Author:        Kim Petersen
-- Created:       00.04.10      last modified: 00.04.13
-- =============================================================================
--  It is a 4 input multiplexer with the function as:
--      sel            Input => output     comments
--     MSB  LSB
--      0    0          in0  => output
--      0    1          in1  => output
--      1    0          in2  => output
--      1    1          in3  => output
--
--------
--    IF   and CASE can only be used inside a process.
--    WHEN and WITH can only be used outside a process.
--
--    IF   corresponds to WHEN
--    CASE correpsonds to WITH
-- =============================================================================

LIBRARY ieee;
USE ieee.std_logic_1164.ALL;      -- can be different dependent on tool used.

ENTITY  mux4_1 IS
   PORT (s0               : IN  STD_LOGIC;
         s1               : IN  STD_LOGIC;
         in0              : IN  STD_LOGIC;
         in1              : IN  STD_LOGIC;
         in2              : IN  STD_LOGIC;
         in3              : IN  STD_LOGIC;
         output           : OUT STD_LOGIC
        );
END mux4_1;

-- =============================================================================
-- =============================================================================
ARCHITECTURE with_example OF mux4_1 IS

SIGNAL  sel  :  STD_LOGIC_VECTOR(1 DOWNTO 0);
-- =============================================================================
BEGIN 
  sel <= s1 & s0;  -- concatenate s1 and s0
  WITH sel  SELECT
    output <= in0 WHEN "00",
              in1 WHEN "01",
              in2 WHEN "10",
              in3 WHEN "11",
              'X' WHEN OTHERS;
     
END with_example;