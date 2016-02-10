/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.util;
import java.io.IOException;

/**
 *  Write a line of text, e.g. a line of error output, in an
 *  appropriate way. For
 *  Text file or printer a reasonable implementation would add
 *  an end-of-line character to the end (and perhaps beginning)
 *  of the text.  For html, enclose in <p> .. </p> instead.
 * @author jrb
 */
public interface LineWriter {
    void writeln(String text) throws IOException;
    void write(String text) throws IOException;
    void flush() throws IOException;
    void close() throws IOException;
}
