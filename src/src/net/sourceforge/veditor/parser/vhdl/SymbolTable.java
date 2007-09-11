/**
 * 
 * This file is based on the VHDL parser originally developed by
 * (c) 1997 Christoph Grimm,
 * J.W. Goethe-University Frankfurt
 * Department for computer engineering
 *
 **/
package net.sourceforge.veditor.parser.vhdl;



import java.util.*;

//
// Classes used for building the symbol tables
//=========================================================



public class SymbolTable
{
  /**
   * Vector, where the symbols are stored.
   */
  Vector<Symbol> symbols = new Vector<Symbol>();

  /**
   * Hierarchie of symbol tables is stored using this
   * variable. The upper symbol table is the enclosing architecture,
   * entity, ... scope. 
   */
  SymbolTable upper_symtab;

  /**
   * Add an identifier to symbol table
   */
  public void addSymbol(Symbol s)
  {
    symbols.addElement(s);
  }

  /**
   * Get a symbol from the symbol table
   */
  public Symbol getSymbol(String identifier)
  {
    int i;
    for ( i = 0; i < symbols.size(); i++ )
    {
      if ( identifier.compareTo(((Symbol)symbols.elementAt(i)).identifier) == 0 )
        return (Symbol) symbols.elementAt(i);
    }
    try {
      return upper_symtab.getSymbol(identifier);
    }
    catch (Exception e)
    {
      return new Symbol("ERROR", 0);
    }
  }


  String block_name;

  /**
   * Start a new Block
   */
  public void newBlock(String identifier)
  {
    block_name = identifier;
  }

  /**
   * End a block with identifier identifier.
   */
  public void endBlock(String identifier)
  {
    if (block_name != identifier)
    {
      System.out.println("ERROR: identifiers at start and end don't match");
    }
  }

  /**
   * dump the symbol table
   */
  public void dump()
  {
    for ( int i = 0; i < symbols.size(); i++ )
    {
      (symbols.elementAt(i)).dump();
    }
  }
}
