/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.util;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 *
 * @author jrb
 */
public class FileLineWriter extends BufferedWriter implements LineWriter {
    private BufferedWriter m_bw = null;
    
    public FileLineWriter(String fileName) throws IOException {
        super(new FileWriter(fileName));
    }
    public void writeln(String text) throws IOException {
        write(text);
        newLine();
    }
}
