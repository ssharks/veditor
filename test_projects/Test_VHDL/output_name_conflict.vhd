library ieee;
use ieee.std_logic_1164.all;

entity tst is
    port  (
        outp : out std_logic
    );
end tst;

architecture arch of tst is
    type r is record
        outp : std_logic;
    end record;
    signal ok_1,ok_2,parsing_problem : r;
begin

    ok_1 <= ( outp => '1'); 

    p : process
    begin 
        ok_2.outp    <= '1';
        parsing_problem <= (outp => '1');
    end process;
end arch;