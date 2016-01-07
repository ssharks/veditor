-- --------------------------------------------------------------------
--
--   Title     :  std_logic_1164 multi-value logic system
--   Library   :  This package shall be compiled into a library 
--             :  symbolically named IEEE.
--             :  
--   Developers:  IEEE model standards group (par 1164)
--   Purpose   :  This packages defines a standard for designers
--             :  to use in describing the interconnection data types
--             :  used in vhdl modeling.
--             : 
--   Limitation:  The logic system defined in this package may
--             :  be insufficient for modeling switched transistors,
--             :  since such a requirement is out of the scope of this
--             :  effort. Furthermore, mathematics, primitives,
--             :  timing standards, etc. are considered orthogonal
--             :  issues as it relates to this package and are therefore
--             :  beyond the scope of this effort.
--             :  
--   Note      :  No declarations or definitions shall be included in,
--             :  or excluded from this package. The "package declaration" 
--             :  defines the types, subtypes and declarations of 
--             :  std_logic_1164. The std_logic_1164 package body shall be 
--             :  considered the formal definition of the semantics of 
--             :  this package. Tool developers may choose to implement 
--             :  the package body in the most efficient manner available 
--             :  to them.
--             :
-- --------------------------------------------------------------------
--   modification history :
-- --------------------------------------------------------------------
--  version | mod. date:| 
--   v4.200 | 01/02/92  | 
--   v4.200 | 02/26/92  | Added Synopsys Synthesis Comments
--   v4.200 | 06/01/92  | Modified the "xnor"s to be xnor functions. 
--	    |           | (see Note bellow)
-- --------------------------------------------------------------------
--
-- Note: Before the VHDL'92 language being officially adopted as
--       containing the "xnor" functions, Synopsys will support
--	 the xnor functions (non-overloaded).
--
--	 GongWen Huang	Synopsys, Inc.
--
--

-- library SYNOPSYS;
-- use SYNOPSYS.ATTRIBUTES.ALL;


PACKAGE std_logic_1164 IS

    -------------------------------------------------------------------    
    -- logic state system  (unresolved)
    -------------------------------------------------------------------    
    TYPE std_ulogic IS ( 'U',  -- Uninitialized
                         'X',  -- Forcing  Unknown
                         '0',  -- Forcing  0
                         '1',  -- Forcing  1
                         'Z',  -- High Impedance   
                         'W',  -- Weak     Unknown
                         'L',  -- Weak     0       
                         'H',  -- Weak     1       
                         '-'   -- Don't care
                       );

--    attribute ENUM_ENCODING of std_ulogic : type is "U D 0 1 Z D 0 1 D";

    -------------------------------------------------------------------    
    -- unconstrained array of std_ulogic for use with the resolution function
    -------------------------------------------------------------------    
    TYPE std_ulogic_vector IS ARRAY ( NATURAL RANGE <> ) OF std_ulogic;
                                    
    -------------------------------------------------------------------    
    -- resolution function
    -------------------------------------------------------------------    
    FUNCTION resolved ( s : std_ulogic_vector ) RETURN std_ulogic;
    --synopsys translate_off
--    attribute REFLEXIVE of resolved: function is TRUE;
--    attribute RESULT_INITIAL_VALUE of resolved: function is std_ulogic'POS('Z');
    --synopsys translate_on


    -------------------------------------------------------------------    
    -- *** industry standard logic type ***
    -------------------------------------------------------------------    
    SUBTYPE std_logic IS resolved std_ulogic;

    -------------------------------------------------------------------    
    -- unconstrained array of std_logic for use in declaring signal arrays
    -------------------------------------------------------------------    
    TYPE std_logic_vector IS ARRAY ( NATURAL RANGE <>) OF std_logic;

    -------------------------------------------------------------------    
    -- common subtypes
    -------------------------------------------------------------------    
    SUBTYPE X01     IS resolved std_ulogic RANGE 'X' TO '1'; -- ('X','0','1') 
    SUBTYPE X01Z    IS resolved std_ulogic RANGE 'X' TO 'Z'; -- ('X','0','1','Z') 
    SUBTYPE UX01    IS resolved std_ulogic RANGE 'U' TO '1'; -- ('U','X','0','1') 
    SUBTYPE UX01Z   IS resolved std_ulogic RANGE 'U' TO 'Z'; -- ('U','X','0','1','Z') 

    -------------------------------------------------------------------    
    -- overloaded logical operators
    -------------------------------------------------------------------    

    FUNCTION "and"  ( l : std_ulogic; r : std_ulogic ) RETURN UX01;
    FUNCTION "nand" ( l : std_ulogic; r : std_ulogic ) RETURN UX01;
    FUNCTION "or"   ( l : std_ulogic; r : std_ulogic ) RETURN UX01;
    FUNCTION "nor"  ( l : std_ulogic; r : std_ulogic ) RETURN UX01;
    FUNCTION "xor"  ( l : std_ulogic; r : std_ulogic ) RETURN UX01;
    function "xnor" ( l : std_ulogic; r : std_ulogic ) return ux01;
    FUNCTION "not"  ( l : std_ulogic                 ) RETURN UX01;
    
    -------------------------------------------------------------------    
    -- vectorized overloaded logical operators
    -------------------------------------------------------------------    
    FUNCTION "and"  ( l, r : std_logic_vector  ) RETURN std_logic_vector;
    FUNCTION "and"  ( l, r : std_ulogic_vector ) RETURN std_ulogic_vector;

    FUNCTION "nand" ( l, r : std_logic_vector  ) RETURN std_logic_vector;
    FUNCTION "nand" ( l, r : std_ulogic_vector ) RETURN std_ulogic_vector;

    FUNCTION "or"   ( l, r : std_logic_vector  ) RETURN std_logic_vector;
    FUNCTION "or"   ( l, r : std_ulogic_vector ) RETURN std_ulogic_vector;

    FUNCTION "nor"  ( l, r : std_logic_vector  ) RETURN std_logic_vector;
    FUNCTION "nor"  ( l, r : std_ulogic_vector ) RETURN std_ulogic_vector;

    FUNCTION "xor"  ( l, r : std_logic_vector  ) RETURN std_logic_vector;
    FUNCTION "xor"  ( l, r : std_ulogic_vector ) RETURN std_ulogic_vector;

--  -----------------------------------------------------------------------
--  Note : The declaration and implementation of the "xnor" function is
--  specifically commented until at which time the VHDL language has been
--  officially adopted as containing such a function. At such a point, 
--  the following comments may be removed along with this notice without
--  further "official" ballotting of this std_logic_1164 package. It is
--  the intent of this effort to provide such a function once it becomes
--  available in the VHDL standard.
--  -----------------------------------------------------------------------
    function "xnor" ( l, r : std_logic_vector  ) return std_logic_vector;
    function "xnor" ( l, r : std_ulogic_vector ) return std_ulogic_vector;
    FUNCTION "not"  ( l : std_logic_vector  ) RETURN std_logic_vector;
    FUNCTION "not"  ( l : std_ulogic_vector ) RETURN std_ulogic_vector;

    -------------------------------------------------------------------
    -- conversion functions
    -------------------------------------------------------------------
    FUNCTION To_bit       ( s : std_ulogic        
    --synopsys synthesis_off
			    ; xmap : BIT := '0'
    --synopsys synthesis_on
			  ) RETURN BIT;

    FUNCTION To_bitvector ( s : std_logic_vector 
    --synopsys synthesis_off
			    ; xmap : BIT := '0'
    --synopsys synthesis_on
			  ) RETURN BIT_VECTOR;

    FUNCTION To_bitvector ( s : std_ulogic_vector
    --synopsys synthesis_off
			    ; xmap : BIT := '0'
    --synopsys synthesis_on
			  ) RETURN BIT_VECTOR;

    FUNCTION To_StdULogic       ( b : BIT               ) RETURN std_ulogic;
    FUNCTION To_StdLogicVector  ( b : BIT_VECTOR        ) RETURN std_logic_vector;
    FUNCTION To_StdLogicVector  ( s : std_ulogic_vector ) RETURN std_logic_vector;
    FUNCTION To_StdULogicVector ( b : BIT_VECTOR        ) RETURN std_ulogic_vector;
    FUNCTION To_StdULogicVector ( s : std_logic_vector  ) RETURN std_ulogic_vector;
    
    -------------------------------------------------------------------    
    -- strength strippers and type convertors
    -------------------------------------------------------------------    

    FUNCTION To_X01  ( s : std_logic_vector  ) RETURN  std_logic_vector;
    FUNCTION To_X01  ( s : std_ulogic_vector ) RETURN  std_ulogic_vector;
    FUNCTION To_X01  ( s : std_ulogic        ) RETURN  X01;
    FUNCTION To_X01  ( b : BIT_VECTOR        ) RETURN  std_logic_vector;
    FUNCTION To_X01  ( b : BIT_VECTOR        ) RETURN  std_ulogic_vector;
    FUNCTION To_X01  ( b : BIT               ) RETURN  X01;       

    FUNCTION To_X01Z ( s : std_logic_vector  ) RETURN  std_logic_vector;
    FUNCTION To_X01Z ( s : std_ulogic_vector ) RETURN  std_ulogic_vector;
    FUNCTION To_X01Z ( s : std_ulogic        ) RETURN  X01Z;
    FUNCTION To_X01Z ( b : BIT_VECTOR        ) RETURN  std_logic_vector;
    FUNCTION To_X01Z ( b : BIT_VECTOR        ) RETURN  std_ulogic_vector;
    FUNCTION To_X01Z ( b : BIT               ) RETURN  X01Z;      

    FUNCTION To_UX01  ( s : std_logic_vector  ) RETURN  std_logic_vector;
    FUNCTION To_UX01  ( s : std_ulogic_vector ) RETURN  std_ulogic_vector;
    FUNCTION To_UX01  ( s : std_ulogic        ) RETURN  UX01;
    FUNCTION To_UX01  ( b : BIT_VECTOR        ) RETURN  std_logic_vector;
    FUNCTION To_UX01  ( b : BIT_VECTOR        ) RETURN  std_ulogic_vector;
    FUNCTION To_UX01  ( b : BIT               ) RETURN  UX01;       

    -------------------------------------------------------------------    
    -- edge detection
    -------------------------------------------------------------------    
    --synopsys synthesis_off
    FUNCTION rising_edge  (SIGNAL s : std_ulogic) RETURN BOOLEAN;
    FUNCTION falling_edge (SIGNAL s : std_ulogic) RETURN BOOLEAN;

    -------------------------------------------------------------------    
    -- object contains an unknown
    -------------------------------------------------------------------    
    FUNCTION Is_X ( s : std_ulogic_vector ) RETURN  BOOLEAN;
    FUNCTION Is_X ( s : std_logic_vector  ) RETURN  BOOLEAN;
    FUNCTION Is_X ( s : std_ulogic        ) RETURN  BOOLEAN;
    --synopsys synthesis_on

END std_logic_1164;


-- --------------------------------------------------------------------
--
--   Title     :  std_logic_1164 multi-value logic system
--   Library   :  This package shall be compiled into a library 
--             :  symbolically named IEEE.
--             :  
--   Developers:  IEEE model standards group (par 1164)
--   Purpose   :  This packages defines a standard for designers
--             :  to use in describing the interconnection data types
--             :  used in vhdl modeling.
--             : 
--   Limitation:  The logic system defined in this package may
--             :  be insufficient for modeling switched transistors,
--             :  since such a requirement is out of the scope of this
--             :  effort. Furthermore, mathematics, primitives,
--             :  timing standards, etc. are considered orthogonal
--             :  issues as it relates to this package and are therefore
--             :  beyond the scope of this effort.
--             :  
--   Note      :  No declarations or definitions shall be included in,
--             :  or excluded from this package. The "package declaration" 
--             :  defines the types, subtypes and declarations of 
--             :  std_logic_1164. The std_logic_1164 package body shall be 
--             :  considered the formal definition of the semantics of 
--             :  this package. Tool developers may choose to implement 
--             :  the package body in the most efficient manner available 
--             :  to them.
--             :
-- --------------------------------------------------------------------
--   modification history :
-- --------------------------------------------------------------------
--  version | mod. date:| 
--   v4.200 | 01/02/91  | 
--   v4.200 |  02/26/92 | Added Synopsys Synthesis Comments
-- --------------------------------------------------------------------

PACKAGE BODY std_logic_1164 IS
    -------------------------------------------------------------------    
    -- local types
    -------------------------------------------------------------------    
    --synopsys synthesis_off 
    TYPE stdlogic_1d IS ARRAY (std_ulogic) OF std_ulogic;
    TYPE stdlogic_table IS ARRAY(std_ulogic, std_ulogic) OF std_ulogic;

    -------------------------------------------------------------------    
    -- resolution function
    -------------------------------------------------------------------    
    CONSTANT resolution_table : stdlogic_table := (
    --      ---------------------------------------------------------
    --      |  U    X    0    1    Z    W    L    H    -        |   |  
    --      ---------------------------------------------------------
            ( 'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U' ), -- | U |
            ( 'U', 'X', 'X', 'X', 'X', 'X', 'X', 'X', 'X' ), -- | X |
            ( 'U', 'X', '0', 'X', '0', '0', '0', '0', 'X' ), -- | 0 |
            ( 'U', 'X', 'X', '1', '1', '1', '1', '1', 'X' ), -- | 1 |
            ( 'U', 'X', '0', '1', 'Z', 'W', 'L', 'H', 'X' ), -- | Z |
            ( 'U', 'X', '0', '1', 'W', 'W', 'W', 'W', 'X' ), -- | W |
            ( 'U', 'X', '0', '1', 'L', 'W', 'L', 'W', 'X' ), -- | L |
            ( 'U', 'X', '0', '1', 'H', 'W', 'W', 'H', 'X' ), -- | H |
            ( 'U', 'X', 'X', 'X', 'X', 'X', 'X', 'X', 'X' )  -- | - |
        );
    --synopsys synthesis_on
        
    FUNCTION resolved ( s : std_ulogic_vector ) RETURN std_ulogic IS
        -- pragma resolution_method three_state
        -- pragma subpgm_id 183
        --synopsys synthesis_off 
        VARIABLE result : std_ulogic := 'Z';  -- weakest state default
        --synopsys synthesis_on    
    BEGIN
        -- the test for a single driver is essential otherwise the
        -- loop would return 'X' for a single driver of '-' and that
        -- would conflict with the value of a single driver unresolved
        -- signal.
        --synopsys synthesis_off
        IF    (s'LENGTH = 1) THEN    RETURN s(s'LOW);
        ELSE
            FOR i IN s'RANGE LOOP
                result := resolution_table(result, s(i));
            END LOOP;
        END IF;
        RETURN result;
        --synopsys synthesis_on
    END resolved;

    -------------------------------------------------------------------    
    -- tables for logical operations
    -------------------------------------------------------------------    

    --synopsys synthesis_off
    -- truth table for "and" function
    CONSTANT and_table : stdlogic_table := (
    --      ----------------------------------------------------
    --      |  U    X    0    1    Z    W    L    H    -         |   |  
    --      ----------------------------------------------------
            ( 'U', 'U', '0', 'U', 'U', 'U', '0', 'U', 'U' ),  -- | U |
            ( 'U', 'X', '0', 'X', 'X', 'X', '0', 'X', 'X' ),  -- | X |
            ( '0', '0', '0', '0', '0', '0', '0', '0', '0' ),  -- | 0 |
            ( 'U', 'X', '0', '1', 'X', 'X', '0', '1', 'X' ),  -- | 1 |
            ( 'U', 'X', '0', 'X', 'X', 'X', '0', 'X', 'X' ),  -- | Z |
            ( 'U', 'X', '0', 'X', 'X', 'X', '0', 'X', 'X' ),  -- | W |
            ( '0', '0', '0', '0', '0', '0', '0', '0', '0' ),  -- | L |
            ( 'U', 'X', '0', '1', 'X', 'X', '0', '1', 'X' ),  -- | H |
            ( 'U', 'X', '0', 'X', 'X', 'X', '0', 'X', 'X' )   -- | - |
    );

    -- truth table for "or" function
    CONSTANT or_table : stdlogic_table := (
    --      ----------------------------------------------------
    --      |  U    X    0    1    Z    W    L    H    -         |   |  
    --      ----------------------------------------------------
            ( 'U', 'U', 'U', '1', 'U', 'U', 'U', '1', 'U' ),  -- | U |
            ( 'U', 'X', 'X', '1', 'X', 'X', 'X', '1', 'X' ),  -- | X |
            ( 'U', 'X', '0', '1', 'X', 'X', '0', '1', 'X' ),  -- | 0 |
            ( '1', '1', '1', '1', '1', '1', '1', '1', '1' ),  -- | 1 |
            ( 'U', 'X', 'X', '1', 'X', 'X', 'X', '1', 'X' ),  -- | Z |
            ( 'U', 'X', 'X', '1', 'X', 'X', 'X', '1', 'X' ),  -- | W |
            ( 'U', 'X', '0', '1', 'X', 'X', '0', '1', 'X' ),  -- | L |
            ( '1', '1', '1', '1', '1', '1', '1', '1', '1' ),  -- | H |
            ( 'U', 'X', 'X', '1', 'X', 'X', 'X', '1', 'X' )   -- | - |
    );

    -- truth table for "xor" function
    CONSTANT xor_table : stdlogic_table := (
    --      ----------------------------------------------------
    --      |  U    X    0    1    Z    W    L    H    -         |   |  
    --      ----------------------------------------------------
            ( 'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U' ),  -- | U |
            ( 'U', 'X', 'X', 'X', 'X', 'X', 'X', 'X', 'X' ),  -- | X |
            ( 'U', 'X', '0', '1', 'X', 'X', '0', '1', 'X' ),  -- | 0 |
            ( 'U', 'X', '1', '0', 'X', 'X', '1', '0', 'X' ),  -- | 1 |
            ( 'U', 'X', 'X', 'X', 'X', 'X', 'X', 'X', 'X' ),  -- | Z |
            ( 'U', 'X', 'X', 'X', 'X', 'X', 'X', 'X', 'X' ),  -- | W |
            ( 'U', 'X', '0', '1', 'X', 'X', '0', '1', 'X' ),  -- | L |
            ( 'U', 'X', '1', '0', 'X', 'X', '1', '0', 'X' ),  -- | H |
            ( 'U', 'X', 'X', 'X', 'X', 'X', 'X', 'X', 'X' )   -- | - |
    );

    -- truth table for "not" function
    CONSTANT not_table: stdlogic_1d := 
    --  -------------------------------------------------
    --  |   U    X    0    1    Z    W    L    H    -   |
    --  -------------------------------------------------
         ( 'U', 'X', '1', '0', 'X', 'X', '1', '0', 'X' ); 
    --synopsys synthesis_on

    -------------------------------------------------------------------    
    -- overloaded logical operators ( with optimizing hints )
    -------------------------------------------------------------------    

    FUNCTION "and"  ( l : std_ulogic; r : std_ulogic ) RETURN UX01 IS
    -- pragma built_in SYN_AND
    -- pragma subpgm_id 184
    BEGIN
    --synopsys synthesis_off
        RETURN (and_table(l, r));
    --synopsys synthesis_on
    END "and";

    FUNCTION "nand" ( l : std_ulogic; r : std_ulogic ) RETURN UX01 IS
    -- pragma built_in SYN_NAND
    -- pragma subpgm_id 185
    BEGIN
    --synopsys synthesis_off
        RETURN  (not_table ( and_table(l, r)));
    --synopsys synthesis_on
    END "nand";

    FUNCTION "or"   ( l : std_ulogic; r : std_ulogic ) RETURN UX01 IS
    -- pragma built_in SYN_OR
    -- pragma subpgm_id 186
    BEGIN
    --synopsys synthesis_off
        RETURN (or_table(l, r));
    --synopsys synthesis_on
    END "or";

    FUNCTION "nor"  ( l : std_ulogic; r : std_ulogic ) RETURN UX01 IS
    -- pragma built_in SYN_NOR
    -- pragma subpgm_id 187
    BEGIN
    --synopsys synthesis_off
        RETURN  (not_table ( or_table( l, r )));
    --synopsys synthesis_on
    END "nor";

    FUNCTION "xor"  ( l : std_ulogic; r : std_ulogic ) RETURN UX01 IS
    -- pragma built_in SYN_XOR
    -- pragma subpgm_id 188
    BEGIN
    --synopsys synthesis_off
        RETURN (xor_table(l, r));
    --synopsys synthesis_on
    END "xor";

  function "xnor"  ( l : std_ulogic; r : std_ulogic ) return ux01 is
  -- pragma built_in SYN_XNOR
  -- pragma subpgm_id 189
  begin
  --synopsys synthesis_off
      return not_table(xor_table(l, r));
  --synopsys synthesis_on
  end "xnor";

--    function xnor  ( l : std_ulogic; r : std_ulogic ) return ux01 is
--    -- pragma built_in SYN_XNOR
--    -- pragma subpgm_id 189
--    begin
--    --synopsys synthesis_off
--        return not_table(xor_table(l, r));
--    --synopsys synthesis_on
--    end xnor;

    FUNCTION "not"  ( l : std_ulogic ) RETURN UX01 IS
    -- pragma built_in SYN_NOT
    -- pragma subpgm_id 190
    BEGIN
    --synopsys synthesis_off
        RETURN (not_table(l));
    --synopsys synthesis_on
    END "not";
    
    -------------------------------------------------------------------    
    -- and
    -------------------------------------------------------------------    
    FUNCTION "and"  ( l,r : std_logic_vector ) RETURN std_logic_vector IS
        -- pragma built_in SYN_AND
	-- pragma subpgm_id 198
        --synopsys synthesis_off
        ALIAS lv : std_logic_vector ( 1 TO l'LENGTH ) IS l;
        ALIAS rv : std_logic_vector ( 1 TO r'LENGTH ) IS r;
        VARIABLE result : std_logic_vector ( 1 TO l'LENGTH );
        --synopsys synthesis_on
    BEGIN
        --synopsys synthesis_off
        IF ( l'LENGTH /= r'LENGTH ) THEN
            ASSERT FALSE
            REPORT "arguments of overloaded 'and' operator are not of the same length"
            SEVERITY FAILURE;
        ELSE
            FOR i IN result'RANGE LOOP
                result(i) := and_table (lv(i), rv(i));
            END LOOP;
        END IF;
        RETURN result;
        --synopsys synthesis_on
    END "and";
    ---------------------------------------------------------------------
    FUNCTION "and"  ( l,r : std_ulogic_vector ) RETURN std_ulogic_vector IS
        -- pragma built_in SYN_AND
	-- pragma subpgm_id 191
        --synopsys synthesis_off
        ALIAS lv : std_ulogic_vector ( 1 TO l'LENGTH ) IS l;
        ALIAS rv : std_ulogic_vector ( 1 TO r'LENGTH ) IS r;
        VARIABLE result : std_ulogic_vector ( 1 TO l'LENGTH );
        --synopsys synthesis_on
    BEGIN
        --synopsys synthesis_off
        IF ( l'LENGTH /= r'LENGTH ) THEN
            ASSERT FALSE
            REPORT "arguments of overloaded 'and' operator are not of the same length"
            SEVERITY FAILURE;
        ELSE
            FOR i IN result'RANGE LOOP
                result(i) := and_table (lv(i), rv(i));
            END LOOP;
        END IF;
        RETURN result;
        --synopsys synthesis_on
    END "and";
    -------------------------------------------------------------------    
    -- nand
    -------------------------------------------------------------------    
    FUNCTION "nand"  ( l,r : std_logic_vector ) RETURN std_logic_vector IS
        -- pragma built_in SYN_NAND
	-- pragma subpgm_id 199
        --synopsys synthesis_off
        ALIAS lv : std_logic_vector ( 1 TO l'LENGTH ) IS l;
        ALIAS rv : std_logic_vector ( 1 TO r'LENGTH ) IS r;
        VARIABLE result : std_logic_vector ( 1 TO l'LENGTH );
        --synopsys synthesis_on
    BEGIN
        --synopsys synthesis_off
        IF ( l'LENGTH /= r'LENGTH ) THEN
            ASSERT FALSE
            REPORT "arguments of overloaded 'nand' operator are not of the same length"
            SEVERITY FAILURE;
        ELSE
            FOR i IN result'RANGE LOOP
                result(i) := not_table(and_table (lv(i), rv(i)));
            END LOOP;
        END IF;
        RETURN result;
        --synopsys synthesis_on
    END "nand";
    ---------------------------------------------------------------------
    FUNCTION "nand"  ( l,r : std_ulogic_vector ) RETURN std_ulogic_vector IS
        -- pragma built_in SYN_NAND
	-- pragma subpgm_id 192
        --synopsys synthesis_off
        ALIAS lv : std_ulogic_vector ( 1 TO l'LENGTH ) IS l;
        ALIAS rv : std_ulogic_vector ( 1 TO r'LENGTH ) IS r;
        VARIABLE result : std_ulogic_vector ( 1 TO l'LENGTH );
        --synopsys synthesis_on
    BEGIN
        --synopsys synthesis_off
        IF ( l'LENGTH /= r'LENGTH ) THEN
            ASSERT FALSE
            REPORT "arguments of overloaded 'nand' operator are not of the same length"
            SEVERITY FAILURE;
        ELSE
            FOR i IN result'RANGE LOOP
                result(i) := not_table(and_table (lv(i), rv(i)));
            END LOOP;
        END IF;
        RETURN result;
        --synopsys synthesis_on
    END "nand";
    -------------------------------------------------------------------    
    -- or
    -------------------------------------------------------------------    
    FUNCTION "or"  ( l,r : std_logic_vector ) RETURN std_logic_vector IS
        -- pragma built_in SYN_OR
	-- pragma subpgm_id 200
        --synopsys synthesis_off
        ALIAS lv : std_logic_vector ( 1 TO l'LENGTH ) IS l;
        ALIAS rv : std_logic_vector ( 1 TO r'LENGTH ) IS r;
        VARIABLE result : std_logic_vector ( 1 TO l'LENGTH );
        --synopsys synthesis_on
    BEGIN
        --synopsys synthesis_off
        IF ( l'LENGTH /= r'LENGTH ) THEN
            ASSERT FALSE
            REPORT "arguments of overloaded 'or' operator are not of the same length"
            SEVERITY FAILURE;
        ELSE
            FOR i IN result'RANGE LOOP
                result(i) := or_table (lv(i), rv(i));
            END LOOP;
        END IF;
        RETURN result;
        --synopsys synthesis_on
    END "or";
    ---------------------------------------------------------------------
    FUNCTION "or"  ( l,r : std_ulogic_vector ) RETURN std_ulogic_vector IS
        -- pragma built_in SYN_OR
	-- pragma subpgm_id 193
        --synopsys synthesis_off
        ALIAS lv : std_ulogic_vector ( 1 TO l'LENGTH ) IS l;
        ALIAS rv : std_ulogic_vector ( 1 TO r'LENGTH ) IS r;
        VARIABLE result : std_ulogic_vector ( 1 TO l'LENGTH );
        --synopsys synthesis_on
    BEGIN
        --synopsys synthesis_off
        IF ( l'LENGTH /= r'LENGTH ) THEN
            ASSERT FALSE
            REPORT "arguments of overloaded 'or' operator are not of the same length"
            SEVERITY FAILURE;
        ELSE
            FOR i IN result'RANGE LOOP
                result(i) := or_table (lv(i), rv(i));
            END LOOP;
        END IF;
        RETURN result;
        --synopsys synthesis_on
    END "or";
    -------------------------------------------------------------------    
    -- nor
    -------------------------------------------------------------------    
    FUNCTION "nor"  ( l,r : std_logic_vector ) RETURN std_logic_vector IS
        -- pragma built_in SYN_NOR
	-- pragma subpgm_id 201
        --synopsys synthesis_off
        ALIAS lv : std_logic_vector ( 1 TO l'LENGTH ) IS l;
        ALIAS rv : std_logic_vector ( 1 TO r'LENGTH ) IS r;
        VARIABLE result : std_logic_vector ( 1 TO l'LENGTH );
        --synopsys synthesis_on
    BEGIN
        --synopsys synthesis_off
        IF ( l'LENGTH /= r'LENGTH ) THEN
            ASSERT FALSE
            REPORT "arguments of overloaded 'nor' operator are not of the same length"
            SEVERITY FAILURE;
        ELSE
            FOR i IN result'RANGE LOOP
                result(i) := not_table(or_table (lv(i), rv(i)));
            END LOOP;
        END IF;
        RETURN result;
        --synopsys synthesis_on
    END "nor";
    ---------------------------------------------------------------------
    FUNCTION "nor"  ( l,r : std_ulogic_vector ) RETURN std_ulogic_vector IS
        -- pragma built_in SYN_NOR
	-- pragma subpgm_id 194
        --synopsys synthesis_off
        ALIAS lv : std_ulogic_vector ( 1 TO l'LENGTH ) IS l;
        ALIAS rv : std_ulogic_vector ( 1 TO r'LENGTH ) IS r;
        VARIABLE result : std_ulogic_vector ( 1 TO l'LENGTH );
        --synopsys synthesis_on
    BEGIN
        --synopsys synthesis_off
        IF ( l'LENGTH /= r'LENGTH ) THEN
            ASSERT FALSE
            REPORT "arguments of overloaded 'nor' operator are not of the same length"
            SEVERITY FAILURE;
        ELSE
            FOR i IN result'RANGE LOOP
                result(i) := not_table(or_table (lv(i), rv(i)));
            END LOOP;
        END IF;
        RETURN result;
        --synopsys synthesis_on
    END "nor";
    ---------------------------------------------------------------------
    -- xor
    -------------------------------------------------------------------    
    FUNCTION "xor"  ( l,r : std_logic_vector ) RETURN std_logic_vector IS
        -- pragma built_in SYN_XOR
	-- pragma subpgm_id 202
        --synopsys synthesis_off
        ALIAS lv : std_logic_vector ( 1 TO l'LENGTH ) IS l;
        ALIAS rv : std_logic_vector ( 1 TO r'LENGTH ) IS r;
        VARIABLE result : std_logic_vector ( 1 TO l'LENGTH );
        --synopsys synthesis_on
    BEGIN
        --synopsys synthesis_off
        IF ( l'LENGTH /= r'LENGTH ) THEN
            ASSERT FALSE
            REPORT "arguments of overloaded 'xor' operator are not of the same length"
            SEVERITY FAILURE;
        ELSE
            FOR i IN result'RANGE LOOP
                result(i) := xor_table (lv(i), rv(i));
            END LOOP;
        END IF;
        RETURN result;
        --synopsys synthesis_on
    END "xor";
    ---------------------------------------------------------------------
    FUNCTION "xor"  ( l,r : std_ulogic_vector ) RETURN std_ulogic_vector IS
        -- pragma built_in SYN_XOR
	-- pragma subpgm_id 195
        --synopsys synthesis_off
        ALIAS lv : std_ulogic_vector ( 1 TO l'LENGTH ) IS l;
        ALIAS rv : std_ulogic_vector ( 1 TO r'LENGTH ) IS r;
        VARIABLE result : std_ulogic_vector ( 1 TO l'LENGTH );
        --synopsys synthesis_on
    BEGIN
        --synopsys synthesis_off
        IF ( l'LENGTH /= r'LENGTH ) THEN
            ASSERT FALSE
            REPORT "arguments of overloaded 'xor' operator are not of the same length"
            SEVERITY FAILURE;
        ELSE
            FOR i IN result'RANGE LOOP
                result(i) := xor_table (lv(i), rv(i));
            END LOOP;
        END IF;
        RETURN result;
        --synopsys synthesis_on
    END "xor";
--  -------------------------------------------------------------------    
--  -- xnor
--  -------------------------------------------------------------------    
--  -----------------------------------------------------------------------
--  Note : The declaration and implementation of the "xnor" function is
--  specifically commented until at which time the VHDL language has been
--  officially adopted as containing such a function. At such a point, 
--  the following comments may be removed along with this notice without
--  further "official" ballotting of this std_logic_1164 package. It is
--  the intent of this effort to provide such a function once it becomes
--  available in the VHDL standard.
--  -----------------------------------------------------------------------
  function "xnor"  ( l,r : std_logic_vector ) return std_logic_vector is
      -- pragma built_in SYN_XNOR
      -- pragma subpgm_id 203
      --synopsys synthesis_off
      alias lv : std_logic_vector ( 1 to l'length ) is l;
      alias rv : std_logic_vector ( 1 to r'length ) is r;
      variable result : std_logic_vector ( 1 to l'length );
      --synopsys synthesis_on
  begin
      --synopsys synthesis_off
      if ( l'length /= r'length ) then
          assert false
          report "arguments of overloaded 'xnor' operator are not of the same length"
          severity failure;
      else
          for i in result'range loop
              result(i) := not_table(xor_table (lv(i), rv(i)));
          end loop;
      end if;
      return result;
      --synopsys synthesis_on
  end "xnor";
--  ---------------------------------------------------------------------
--  function xnor  ( l,r : std_ulogic_vector ) return std_ulogic_vector is
--      -- pragma built_in SYN_XNOR
--      -- pragma subpgm_id 196
--      --synopsys synthesis_off
--      alias lv : std_ulogic_vector ( 1 to l'length ) is l;
--      alias rv : std_ulogic_vector ( 1 to r'length ) is r;
--      variable result : std_ulogic_vector ( 1 to l'length );
--      --synopsys synthesis_on
--  begin
--      --synopsys synthesis_off
--      if ( l'length /= r'length ) then
--          assert false
--          report "arguments of overloaded 'xnor' operator are not of the same length"
--          severity failure;
--      else
--          for i in result'range loop
--              result(i) := not_table(xor_table (lv(i), rv(i)));
--          end loop;
--      end if;
--      return result;
--      --synopsys synthesis_on
--  end "xnor";

--    function xnor  ( l,r : std_logic_vector ) return std_logic_vector is
--        -- pragma built_in SYN_XNOR
--	-- pragma subpgm_id 203
--        --synopsys synthesis_off
--        alias lv : std_logic_vector ( 1 to l'length ) is l;
--        alias rv : std_logic_vector ( 1 to r'length ) is r;
--        variable result : std_logic_vector ( 1 to l'length );
--        --synopsys synthesis_on
--    begin
--        --synopsys synthesis_off
--        if ( l'length /= r'length ) then
--            assert false
--            report "arguments of overloaded 'xnor' operator are not of the same length"
--            severity failure;
--        else
--            for i in result'range loop
--                result(i) := not_table(xor_table (lv(i), rv(i)));
--            end loop;
--        end if;
--        return result;
--        --synopsys synthesis_on
--    end xnor;
    ---------------------------------------------------------------------
    function "xnor"  ( l,r : std_ulogic_vector ) return std_ulogic_vector is
        -- pragma built_in SYN_XNOR
	-- pragma subpgm_id 196
        --synopsys synthesis_off
        alias lv : std_ulogic_vector ( 1 to l'length ) is l;
        alias rv : std_ulogic_vector ( 1 to r'length ) is r;
        variable result : std_ulogic_vector ( 1 to l'length );
        --synopsys synthesis_on
    begin
        --synopsys synthesis_off
        if ( l'length /= r'length ) then
            assert false
            report "arguments of overloaded 'xnor' operator are not of the same length"
            severity failure;
        else
            for i in result'range loop
                result(i) := not_table(xor_table (lv(i), rv(i)));
            end loop;
        end if;
        return result;
        --synopsys synthesis_on
    end "xnor";



    -------------------------------------------------------------------    
    -- not
    -------------------------------------------------------------------    
    FUNCTION "not"  ( l : std_logic_vector ) RETURN std_logic_vector IS
        -- pragma built_in SYN_NOT
	-- pragma subpgm_id 204
        --synopsys synthesis_off
        ALIAS lv : std_logic_vector ( 1 TO l'LENGTH ) IS l;
        VARIABLE result : std_logic_vector ( 1 TO l'LENGTH ) := (OTHERS => 'X');
        --synopsys synthesis_on
    BEGIN
        --synopsys synthesis_off
        FOR i IN result'RANGE LOOP
            result(i) := not_table( lv(i) );
        END LOOP;
        RETURN result;
        --synopsys synthesis_on
    END;
    ---------------------------------------------------------------------
    FUNCTION "not"  ( l : std_ulogic_vector ) RETURN std_ulogic_vector IS
        -- pragma built_in SYN_NOT
	-- pragma subpgm_id 197
        --synopsys synthesis_off
        ALIAS lv : std_ulogic_vector ( 1 TO l'LENGTH ) IS l;
        VARIABLE result : std_ulogic_vector ( 1 TO l'LENGTH ) := (OTHERS => 'X');
        --synopsys synthesis_on
    BEGIN
        --synopsys synthesis_off
        FOR i IN result'RANGE LOOP
            result(i) := not_table( lv(i) );
        END LOOP;
        RETURN result;
        --synopsys synthesis_on
    END;
    -------------------------------------------------------------------    
    -- conversion tables
    -------------------------------------------------------------------    
    --synopsys synthesis_off

    TYPE logic_x01_table IS ARRAY (std_ulogic'LOW TO std_ulogic'HIGH) OF X01;
    TYPE logic_x01z_table IS ARRAY (std_ulogic'LOW TO std_ulogic'HIGH) OF X01Z;
    TYPE logic_ux01_table IS ARRAY (std_ulogic'LOW TO std_ulogic'HIGH) OF UX01;
    ----------------------------------------------------------
    -- table name : cvt_to_x01
    --
    -- parameters :
    --        in  :  std_ulogic  -- some logic value
    -- returns    :  x01         -- state value of logic value
    -- purpose    :  to convert state-strength to state only
    --                  
    -- example    : if (cvt_to_x01 (input_signal) = '1' ) then ...
    --
    ----------------------------------------------------------
    CONSTANT cvt_to_x01 : logic_x01_table := (
                         'X',  -- 'U'
                         'X',  -- 'X'
                         '0',  -- '0'
                         '1',  -- '1'
                         'X',  -- 'Z'
                         'X',  -- 'W'
                         '0',  -- 'L'
                         '1',  -- 'H'
                         'X'   -- '-'
                        );

    ----------------------------------------------------------
    -- table name : cvt_to_x01z
    --
    -- parameters :
    --        in  :  std_ulogic  -- some logic value
    -- returns    :  x01z        -- state value of logic value
    -- purpose    :  to convert state-strength to state only
    --                  
    -- example    : if (cvt_to_x01z (input_signal) = '1' ) then ...
    --
    ----------------------------------------------------------
    CONSTANT cvt_to_x01z : logic_x01z_table := (
                         'X',  -- 'U'
                         'X',  -- 'X'
                         '0',  -- '0'
                         '1',  -- '1'
                         'Z',  -- 'Z'
                         'X',  -- 'W'
                         '0',  -- 'L'
                         '1',  -- 'H'
                         'X'   -- '-'
                        );

    ----------------------------------------------------------
    -- table name : cvt_to_ux01
    --
    -- parameters :
    --        in  :  std_ulogic  -- some logic value
    -- returns    :  ux01        -- state value of logic value
    -- purpose    :  to convert state-strength to state only
    --                  
    -- example    : if (cvt_to_ux01 (input_signal) = '1' ) then ...
    --
    ----------------------------------------------------------
    CONSTANT cvt_to_ux01 : logic_ux01_table := (
                         'U',  -- 'U'
                         'X',  -- 'X'
                         '0',  -- '0'
                         '1',  -- '1'
                         'X',  -- 'Z'
                         'X',  -- 'W'
                         '0',  -- 'L'
                         '1',  -- 'H'
                         'X'   -- '-'
                        );
    --synopsys synthesis_on
    
    -------------------------------------------------------------------    
    -- conversion functions
    -------------------------------------------------------------------    
    FUNCTION To_bit       ( s : std_ulogic        
    --synopsys synthesis_off
			    ; xmap : BIT := '0'
    --synopsys synthesis_on
			  ) RETURN BIT IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 205
    BEGIN
    --synopsys synthesis_off
            CASE s IS
                WHEN '0' | 'L' => RETURN ('0');
                WHEN '1' | 'H' => RETURN ('1');
                WHEN OTHERS => RETURN xmap;
            END CASE;
    --synopsys synthesis_on
    END;
    --------------------------------------------------------------------
    FUNCTION To_bitvector ( s : std_logic_vector 
    --synopsys synthesis_off
			    ; xmap : BIT := '0'
    --synopsys synthesis_on
			  ) RETURN BIT_VECTOR IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 206
    --synopsys synthesis_off
        ALIAS sv : std_logic_vector ( s'LENGTH-1 DOWNTO 0 ) IS s;
        VARIABLE result : BIT_VECTOR ( s'LENGTH-1 DOWNTO 0 );
    --synopsys synthesis_on
    BEGIN
    --synopsys synthesis_off
        FOR i IN result'RANGE LOOP
            CASE sv(i) IS
                WHEN '0' | 'L' => result(i) := '0';
                WHEN '1' | 'H' => result(i) := '1';
                WHEN OTHERS => result(i) := xmap;
            END CASE;
        END LOOP;
        RETURN result;
    --synopsys synthesis_on
    END;
    --------------------------------------------------------------------
    FUNCTION To_bitvector ( s : std_ulogic_vector
    --synopsys synthesis_off
			    ; xmap : BIT := '0'
    --synopsys synthesis_on
			  ) RETURN BIT_VECTOR IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 207
    --synopsys synthesis_off
        ALIAS sv : std_ulogic_vector ( s'LENGTH-1 DOWNTO 0 ) IS s;
        VARIABLE result : BIT_VECTOR ( s'LENGTH-1 DOWNTO 0 );
    --synopsys synthesis_on
    BEGIN
    --synopsys synthesis_off
        FOR i IN result'RANGE LOOP
            CASE sv(i) IS
                WHEN '0' | 'L' => result(i) := '0';
                WHEN '1' | 'H' => result(i) := '1';
                WHEN OTHERS => result(i) := xmap;
            END CASE;
        END LOOP;
        RETURN result;
    --synopsys synthesis_on
    END;
    --------------------------------------------------------------------
    FUNCTION To_StdULogic       ( b : BIT               ) RETURN std_ulogic IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 208
    BEGIN
    --synopsys synthesis_off
        CASE b IS
            WHEN '0' => RETURN '0';
            WHEN '1' => RETURN '1';
        END CASE;
    --synopsys synthesis_on
    END;
    --------------------------------------------------------------------
    FUNCTION To_StdLogicVector  ( b : BIT_VECTOR        ) RETURN std_logic_vector IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 209
    --synopsys synthesis_off
        ALIAS bv : BIT_VECTOR ( b'LENGTH-1 DOWNTO 0 ) IS b;
        VARIABLE result : std_logic_vector ( b'LENGTH-1 DOWNTO 0 );
    --synopsys synthesis_on
    BEGIN
    --synopsys synthesis_off
        FOR i IN result'RANGE LOOP
            CASE bv(i) IS
                WHEN '0' => result(i) := '0';
                WHEN '1' => result(i) := '1';
            END CASE;
        END LOOP;
        RETURN result;
    --synopsys synthesis_on
    END;
    --------------------------------------------------------------------
    FUNCTION To_StdLogicVector  ( s : std_ulogic_vector ) RETURN std_logic_vector IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 210
    --synopsys synthesis_off
        ALIAS sv : std_ulogic_vector ( s'LENGTH-1 DOWNTO 0 ) IS s;
        VARIABLE result : std_logic_vector ( s'LENGTH-1 DOWNTO 0 );
    --synopsys synthesis_on
    BEGIN
    --synopsys synthesis_off
        FOR i IN result'RANGE LOOP
            result(i) := sv(i);
        END LOOP;
        RETURN result;
    --synopsys synthesis_on
    END;
    --------------------------------------------------------------------
    FUNCTION To_StdULogicVector ( b : BIT_VECTOR        ) RETURN std_ulogic_vector IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 211
    --synopsys synthesis_off
        ALIAS bv : BIT_VECTOR ( b'LENGTH-1 DOWNTO 0 ) IS b;
        VARIABLE result : std_ulogic_vector ( b'LENGTH-1 DOWNTO 0 );
    --synopsys synthesis_on
    BEGIN
    --synopsys synthesis_off
        FOR i IN result'RANGE LOOP
            CASE bv(i) IS
                WHEN '0' => result(i) := '0';
                WHEN '1' => result(i) := '1';
            END CASE;
        END LOOP;
        RETURN result;
    --synopsys synthesis_on
    END;
    --------------------------------------------------------------------
    FUNCTION To_StdULogicVector ( s : std_logic_vector ) RETURN std_ulogic_vector IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 212
    --synopsys synthesis_off
        ALIAS sv : std_logic_vector ( s'LENGTH-1 DOWNTO 0 ) IS s;
        VARIABLE result : std_ulogic_vector ( s'LENGTH-1 DOWNTO 0 );
    --synopsys synthesis_on
    BEGIN
    --synopsys synthesis_off
        FOR i IN result'RANGE LOOP
            result(i) := sv(i);
        END LOOP;
        RETURN result;
    --synopsys synthesis_on
    END;
    
    -------------------------------------------------------------------    
    -- strength strippers and type convertors
    -------------------------------------------------------------------    
    -- to_x01
    -------------------------------------------------------------------    
    FUNCTION To_X01  ( s : std_logic_vector ) RETURN  std_logic_vector IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 213
    --synopsys synthesis_off
        ALIAS sv : std_logic_vector ( 1 TO s'LENGTH ) IS s;
        VARIABLE result : std_logic_vector ( 1 TO s'LENGTH );
    --synopsys synthesis_on
    BEGIN
    --synopsys synthesis_off
        FOR i IN result'RANGE LOOP
            result(i) := cvt_to_x01 (sv(i));
        END LOOP;
        RETURN result;
    --synopsys synthesis_on
    END;
    --------------------------------------------------------------------
    FUNCTION To_X01  ( s : std_ulogic_vector ) RETURN  std_ulogic_vector IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 214
    --synopsys synthesis_off
        ALIAS sv : std_ulogic_vector ( 1 TO s'LENGTH ) IS s;
        VARIABLE result : std_ulogic_vector ( 1 TO s'LENGTH );
    --synopsys synthesis_on
    BEGIN
    --synopsys synthesis_off
        FOR i IN result'RANGE LOOP
            result(i) := cvt_to_x01 (sv(i));
        END LOOP;
        RETURN result;
    --synopsys synthesis_on
    END;
    --------------------------------------------------------------------
    FUNCTION To_X01  ( s : std_ulogic ) RETURN  X01 IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 215
    BEGIN
    --synopsys synthesis_off
        RETURN (cvt_to_x01(s));
    --synopsys synthesis_on
    END;
    --------------------------------------------------------------------
    FUNCTION To_X01  ( b : BIT_VECTOR ) RETURN  std_logic_vector IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 216
    --synopsys synthesis_off
        ALIAS bv : BIT_VECTOR ( 1 TO b'LENGTH ) IS b;
        VARIABLE result : std_logic_vector ( 1 TO b'LENGTH );
    --synopsys synthesis_on
    BEGIN
    --synopsys synthesis_off
        FOR i IN result'RANGE LOOP
            CASE bv(i) IS
                WHEN '0' => result(i) := '0';
                WHEN '1' => result(i) := '1';
            END CASE;
        END LOOP;
        RETURN result;
    --synopsys synthesis_on
    END;
    --------------------------------------------------------------------
    FUNCTION To_X01  ( b : BIT_VECTOR ) RETURN  std_ulogic_vector IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 217
    --synopsys synthesis_off
        ALIAS bv : BIT_VECTOR ( 1 TO b'LENGTH ) IS b;
        VARIABLE result : std_ulogic_vector ( 1 TO b'LENGTH );
    --synopsys synthesis_on
    BEGIN
    --synopsys synthesis_off
        FOR i IN result'RANGE LOOP
            CASE bv(i) IS
                WHEN '0' => result(i) := '0';
                WHEN '1' => result(i) := '1';
            END CASE;
        END LOOP;
        RETURN result;
    --synopsys synthesis_on
    END;
    --------------------------------------------------------------------
    FUNCTION To_X01  ( b : BIT ) RETURN  X01 IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 218
    BEGIN
    --synopsys synthesis_off
            CASE b IS
                WHEN '0' => RETURN('0');
                WHEN '1' => RETURN('1');              
            END CASE;
    --synopsys synthesis_on
    END;
    --------------------------------------------------------------------
    -- to_x01z
    -------------------------------------------------------------------    
    FUNCTION To_X01Z  ( s : std_logic_vector ) RETURN  std_logic_vector IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 219
    --synopsys synthesis_off
        ALIAS sv : std_logic_vector ( 1 TO s'LENGTH ) IS s;
        VARIABLE result : std_logic_vector ( 1 TO s'LENGTH );
    --synopsys synthesis_on
    BEGIN
    --synopsys synthesis_off
        FOR i IN result'RANGE LOOP
            result(i) := cvt_to_x01z (sv(i));
        END LOOP;
        RETURN result;
    --synopsys synthesis_on
    END;
    --------------------------------------------------------------------
    FUNCTION To_X01Z  ( s : std_ulogic_vector ) RETURN  std_ulogic_vector IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 220
    --synopsys synthesis_off
        ALIAS sv : std_ulogic_vector ( 1 TO s'LENGTH ) IS s;
        VARIABLE result : std_ulogic_vector ( 1 TO s'LENGTH );
    --synopsys synthesis_on
    BEGIN
    --synopsys synthesis_off
        FOR i IN result'RANGE LOOP
            result(i) := cvt_to_x01z (sv(i));
        END LOOP;
        RETURN result;
    --synopsys synthesis_on
    END;
    --------------------------------------------------------------------
    FUNCTION To_X01Z  ( s : std_ulogic ) RETURN  X01Z IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 221
    BEGIN
    --synopsys synthesis_off
        RETURN (cvt_to_x01z(s));
    --synopsys synthesis_on
    END;
    --------------------------------------------------------------------
    FUNCTION To_X01Z  ( b : BIT_VECTOR ) RETURN  std_logic_vector IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 222
    --synopsys synthesis_off
        ALIAS bv : BIT_VECTOR ( 1 TO b'LENGTH ) IS b;
        VARIABLE result : std_logic_vector ( 1 TO b'LENGTH );
    --synopsys synthesis_on
    BEGIN
    --synopsys synthesis_off
        FOR i IN result'RANGE LOOP
            CASE bv(i) IS
                WHEN '0' => result(i) := '0';
                WHEN '1' => result(i) := '1';
            END CASE;
        END LOOP;
        RETURN result;
    --synopsys synthesis_on
    END;
    --------------------------------------------------------------------
    FUNCTION To_X01Z  ( b : BIT_VECTOR ) RETURN  std_ulogic_vector IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 223
    --synopsys synthesis_off
        ALIAS bv : BIT_VECTOR ( 1 TO b'LENGTH ) IS b;
        VARIABLE result : std_ulogic_vector ( 1 TO b'LENGTH );
    --synopsys synthesis_on
    BEGIN
    --synopsys synthesis_off
        FOR i IN result'RANGE LOOP
            CASE bv(i) IS
                WHEN '0' => result(i) := '0';
                WHEN '1' => result(i) := '1';
            END CASE;
        END LOOP;
        RETURN result;
    --synopsys synthesis_on
    END;
    --------------------------------------------------------------------
    FUNCTION To_X01Z  ( b : BIT ) RETURN  X01Z IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 224
    BEGIN
    --synopsys synthesis_off
            CASE b IS
                WHEN '0' => RETURN('0');
                WHEN '1' => RETURN('1');              
            END CASE;
    --synopsys synthesis_on
    END;
    --------------------------------------------------------------------
    -- to_ux01
    -------------------------------------------------------------------    
    FUNCTION To_UX01  ( s : std_logic_vector ) RETURN  std_logic_vector IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 225
    --synopsys synthesis_off
        ALIAS sv : std_logic_vector ( 1 TO s'LENGTH ) IS s;
        VARIABLE result : std_logic_vector ( 1 TO s'LENGTH );
    --synopsys synthesis_on
    BEGIN
    --synopsys synthesis_off
        FOR i IN result'RANGE LOOP
            result(i) := cvt_to_ux01 (sv(i));
        END LOOP;
        RETURN result;
    --synopsys synthesis_on
    END;
    --------------------------------------------------------------------
    FUNCTION To_UX01  ( s : std_ulogic_vector ) RETURN  std_ulogic_vector IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 226
    --synopsys synthesis_off
        ALIAS sv : std_ulogic_vector ( 1 TO s'LENGTH ) IS s;
        VARIABLE result : std_ulogic_vector ( 1 TO s'LENGTH );
    --synopsys synthesis_on
    BEGIN
    --synopsys synthesis_off
        FOR i IN result'RANGE LOOP
            result(i) := cvt_to_ux01 (sv(i));
        END LOOP;
        RETURN result;
    --synopsys synthesis_on
    END;
    --------------------------------------------------------------------
    FUNCTION To_UX01  ( s : std_ulogic ) RETURN  UX01 IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 227
    BEGIN
    --synopsys synthesis_off
        RETURN (cvt_to_ux01(s));
    --synopsys synthesis_on
    END;
    --------------------------------------------------------------------
    FUNCTION To_UX01  ( b : BIT_VECTOR ) RETURN  std_logic_vector IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 228
    --synopsys synthesis_off
        ALIAS bv : BIT_VECTOR ( 1 TO b'LENGTH ) IS b;
        VARIABLE result : std_logic_vector ( 1 TO b'LENGTH );
    --synopsys synthesis_on
    BEGIN
    --synopsys synthesis_off
        FOR i IN result'RANGE LOOP
            CASE bv(i) IS
                WHEN '0' => result(i) := '0';
                WHEN '1' => result(i) := '1';
            END CASE;
        END LOOP;
        RETURN result;
    --synopsys synthesis_on
    END;
    --------------------------------------------------------------------
    FUNCTION To_UX01  ( b : BIT_VECTOR ) RETURN  std_ulogic_vector IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 229
    --synopsys synthesis_off
        ALIAS bv : BIT_VECTOR ( 1 TO b'LENGTH ) IS b;
        VARIABLE result : std_ulogic_vector ( 1 TO b'LENGTH );
    --synopsys synthesis_on
    BEGIN
    --synopsys synthesis_off
        FOR i IN result'RANGE LOOP
            CASE bv(i) IS
                WHEN '0' => result(i) := '0';
                WHEN '1' => result(i) := '1';
            END CASE;
        END LOOP;
        RETURN result;
    --synopsys synthesis_on
    END;
    --------------------------------------------------------------------
    FUNCTION To_UX01  ( b : BIT ) RETURN  UX01 IS
    -- pragma built_in SYN_FEED_THRU
    -- pragma subpgm_id 230
    BEGIN
    --synopsys synthesis_off
            CASE b IS
                WHEN '0' => RETURN('0');
                WHEN '1' => RETURN('1');              
            END CASE;
    --synopsys synthesis_on
    END;

    -------------------------------------------------------------------    
    -- edge detection
    -------------------------------------------------------------------    
    --synopsys synthesis_off
    FUNCTION rising_edge  (SIGNAL s : std_ulogic) RETURN BOOLEAN IS
    -- pragma subpgm_id 231
    BEGIN
        RETURN (s'EVENT AND (To_X01(s) = '1') AND 
                            (To_X01(s'LAST_VALUE) = '0'));
    END;

    FUNCTION falling_edge (SIGNAL s : std_ulogic) RETURN BOOLEAN IS
    -- pragma subpgm_id 232
    BEGIN
        RETURN (s'EVENT AND (To_X01(s) = '0') AND 
                            (To_X01(s'LAST_VALUE) = '1'));
    END;

    -------------------------------------------------------------------    
    -- object contains an unknown
    -------------------------------------------------------------------    
    FUNCTION Is_X ( s : std_ulogic_vector ) RETURN  BOOLEAN IS
    -- pragma subpgm_id 233
    BEGIN
        FOR i IN s'RANGE LOOP
            CASE s(i) IS
                WHEN 'U' | 'X' | 'Z' | 'W' | '-' => RETURN TRUE;
                WHEN OTHERS => NULL;
            END CASE;
        END LOOP;
        RETURN FALSE;
    END;
    --------------------------------------------------------------------
    FUNCTION Is_X ( s : std_logic_vector  ) RETURN  BOOLEAN IS
    -- pragma subpgm_id 234
    BEGIN
        FOR i IN s'RANGE LOOP
            CASE s(i) IS
                WHEN 'U' | 'X' | 'Z' | 'W' | '-' => RETURN TRUE;
                WHEN OTHERS => NULL;
            END CASE;
        END LOOP;
        RETURN FALSE;
    END;
    --------------------------------------------------------------------
    FUNCTION Is_X ( s : std_ulogic        ) RETURN  BOOLEAN IS
    -- pragma subpgm_id 235
    BEGIN
        CASE s IS
            WHEN 'U' | 'X' | 'Z' | 'W' | '-' => RETURN TRUE;
            WHEN OTHERS => NULL;
        END CASE;
        RETURN FALSE;
    END;
    --synopsys synthesis_on

END std_logic_1164;
