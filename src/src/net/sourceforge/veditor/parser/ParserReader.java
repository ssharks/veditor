/*******************************************************************************
 * Copyright (c) 2011 VEditor Team
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ali Ghorashi - initial API and implementation
 *    KOBAYASHI Tadashi - add initialize method used from VerilogParserReader
 *******************************************************************************/
package net.sourceforge.veditor.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

/**
 * This class overrides the basic function of input stream ready
 * and can optionally stop reading the stream when the stop method
 * is called. It is used to terminate parsing of large files
 * that exceed maximum parsing parameters
 * 
 * @author gho18481
 *
 */
public class ParserReader extends Reader {

    protected boolean bStopReading;
    protected Reader reader;
    
    public ParserReader() {
    	bStopReading=false;
    }

    public ParserReader(InputStream in) {
        reader =new InputStreamReader(in);
        bStopReading=false;
    }   
    
    public ParserReader(String text) {
    	initialize(text);
    } 
    
    public void initialize(String text) {
    	reader =new StringReader(text);
        bStopReading=false;
    }

    @Override
    public void close() throws IOException {
      reader.close();        
    }
    
    @Override
    public void mark(int readAheadLimit) throws IOException{
        reader.mark(readAheadLimit);
    }
    
    @Override
    public boolean markSupported(){
        return reader.markSupported();
    }
    
    @Override
    public int read() throws IOException {
        if(bStopReading) return -1;
        return reader.read();
    }
    
    @Override
    public int read(char[] cbuf) throws IOException {
        int nResults;
        
        if(bStopReading){ 
            nResults=-1;
        }
        else{
            nResults=super.read(cbuf);        
            if(nResults != -1){
                sanitize(cbuf);
            }
        }        
        return nResults;
    }
    
    @Override
    public int read(char[] cbuf, int offset, int length) throws IOException {
        int nResults;
        
        if(bStopReading){           
            nResults=-1;
        }
        else{
            nResults=reader.read(cbuf, offset, length);    
            if(nResults != -1){
                sanitize(cbuf);
            }
        }
        
        return nResults;
    }
    
    @Override
    public boolean ready() throws IOException{
        return reader.ready();
    }
    
    @Override
    public void reset() throws IOException{
        bStopReading=false;
        reader.reset();
    }
    
    @Override
    public long skip(long n) throws IOException{
        return reader.skip(n);
    }
    /** 
     * Stops the reader from reading the stream any further
     */
    public void stop(){
        bStopReading=true; 
    }
       
  
    /**
     * Removes two byte characters from the character array
     * and replaces them with a space
     * @param cbuf The character array to scrub
     */
    protected void sanitize(char [] cbuf){
        for(int i = 0; i < cbuf.length; i++)
        {
            if (cbuf[i] >= 0x0100)
                cbuf[i] = ' ';
        }
    }
}
