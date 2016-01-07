-------------------------------------------------------------------------------
--
-- (C) COPYRIGHT 2009 TECHNOLUTION BV, GOUDA NL
-- | =======          I                   ==          I    =
-- |    I             I                    I          I
-- |    I   ===   === I ===  I ===   ===   I  I    I ====  I   ===  I ===
-- |    I  /   \ I    I/   I I/   I I   I  I  I    I  I    I  I   I I/   I
-- |    I  ===== I    I    I I    I I   I  I  I    I  I    I  I   I I    I
-- |    I  \     I    I    I I    I I   I  I  I   /I  \    I  I   I I    I
-- |    I   ===   === I    I I    I  ===  ===  === I   ==  I   ===  I    I
-- |                 +---------------------------------------------------+
-- +----+            |  +++++++++++++++++++++++++++++++++++++++++++++++++|
--      |            |             ++++++++++++++++++++++++++++++++++++++|
--      +------------+                          +++++++++++++++++++++++++|
--                                                         ++++++++++++++|
--              A U T O M A T I O N     T E C H N O L O G Y         +++++|
--
-------------------------------------------------------------------------------
-- Title      : uart_bfm_pkg
-- Author     : Edwin Hakkennes  <Edwin.Hakkennes@Technolution.NL>
-------------------------------------------------------------------------------
-- Description: control package for the uart_bfm
-------------------------------------------------------------------------------
library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

package uart_bfm_pkg is
    
    procedure get_byte_from_buffer(
        variable buf : inout t_uart_bfm_buffer;
        variable b   : inout std_logic_vector);

    procedure put_byte_in_buffer(
        variable buf : inout t_uart_bfm_buffer;
        b            :       std_logic_vector);

    procedure wait_for_data_in_buffer(
        variable buf : inout t_uart_bfm_buffer;
        x            :       natural;
        interval     :       time := 1 us);

    procedure get_byte(
        variable p : inout ta_uart_bfm_object;
        b          : inout std_logic_vector);

    procedure get_byte(
        b          : inout std_logic_vector);

    procedure send_byte(
        variable p : inout ta_uart_bfm_object;
        b          : in    std_logic_vector);

    ---------------------------------------------------------------------------
    -- procedures when only one bfm is present.
    ---------------------------------------------------------------------------
    
end;

package body uart_bfm_pkg is

    procedure get_byte_from_buffer(
        variable buf : inout t_uart_bfm_buffer;
        variable b   : inout std_logic_vector) is
    begin
        if buf.count = 0 then
            b := X"00";
        else
            buf.count      := buf.count - 1;
            b              := buf.data(buf.rd_pointer);
            buf.rd_pointer := buf.rd_pointer + 1;
            if buf.rd_pointer = 256 then
                buf.rd_pointer := 0;
            end if;
        end if;
    end get_byte_from_buffer;
end;



