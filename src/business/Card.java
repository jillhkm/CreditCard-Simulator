/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author jilln
 */
public class Card {
    private double climit, baldue;
    private long acctno;
    private String errmsg, actionmsg;
    
    public Card() {
        this.climit = 0;
        this.baldue = 0;
        this.acctno = 0;
        this.errmsg = "";
        this.actionmsg = "";
        
        while (this.acctno == 0) {
            try {
                this.acctno = (long) (Math.random() * 1000000);
                BufferedReader in = new BufferedReader (
                                new FileReader("CC" + this.acctno + ".txt"));
                //success is a bad outcome: file already exists
                in.close();
                this.acctno = 0;
            } catch (FileNotFoundException e) {
                //success! acctno okay
                this.climit = 1000;
                if (writeStatus()){
                   this.actionmsg = "Account " + this.acctno + " opened.";
                   writeLog(this.actionmsg);
                } else {
                    this.acctno = 0;
                    this.climit = 0;
                }
            } catch (IOException e) {
                this.acctno = 0;
            }
        }//end of while
    } //end of constructor
   
    private boolean writeStatus() {
        try {
            PrintWriter out = new PrintWriter(
                    new FileWriter("CC" + this.acctno + ".txt"));
            out.println(this.climit);
            out.println(this.baldue);
            out.close();
            return true;
        } catch (IOException e ){
            this.errmsg = "Unable to write CC file for " + this.acctno;
            return false;
        }
    }//end of writeStatus
    
    private void writeLog(String msg) {
        try {
            //true = don't overwrite, just append
            PrintWriter out = new PrintWriter(
                    new FileWriter("CCL"+this.acctno+".txt",true));
            out.println(msg);
            out.close();
        } catch (IOException e) {
            this.errmsg = "Unable to update log for: " + this.acctno;
        }
    }
    
    public String getErrorMsg() {
        return this.errmsg;
    }
    
    public long getAcctNo() {
        return this.acctno;
    }
}//end of class
