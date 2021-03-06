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
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

/**
 *
 * @author jilln
 */
public class Card {
    private double climit, baldue;
    private long acctno = 0;
    private String errmsg, actionmsg;
    NumberFormat curr = NumberFormat.getCurrencyInstance();
    
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
            Calendar cal = Calendar.getInstance();
            DateFormat df = DateFormat.getDateInstance();
            String ts = df.format(cal.getTime());
//true = don't overwrite, just append
            PrintWriter out = new PrintWriter(
                    new FileWriter("CCL"+this.acctno+".txt",true));
            out.println(ts + ": " + msg);
            out.close();
        } catch (IOException e) {
            this.errmsg = "Unable to update log for: " + this.acctno;
        }
    }
    
    public String getErrorMsg() {
        return this.errmsg;
    }
    
    public String getActionMsg() {
        return this.actionmsg;
    }
    
    public long getAcctNo() {
        return this.acctno;
    }
    
    public double getCreditLimit() {
        return this.climit;
    }
    
    public double getBalDue() {
        return this.baldue;
    }
  
    public double getCrAvail() {
        return (this.climit - this.baldue);
    }
    
    public void setCharge(double amt, String desc) {
        this.errmsg = "";
        this.actionmsg = "";
        if (!(this.acctno > 0)) {
            this.errmsg = "Charge attempted on unopened account.";
            return;
        }
        if (amt <= 0) {
            this.actionmsg = "Charge declined: Charge amount cannot be negative";
            writeLog(this.actionmsg);
        } else if (desc.isEmpty()) {
            this.actionmsg = "Charge declined: must have a description";
            writeLog(this.actionmsg);
        } else if ((this.baldue + amt) > this.climit) {
            this.actionmsg = "Charge declined: over credit limit";
            writeLog(this.actionmsg);
        } else {
            this.baldue += amt;
            if (writeStatus()) {
                this.actionmsg = "Charge of " + curr.format(amt) + " for " + desc
                        + " posted.";
                writeLog(this.actionmsg);
            } else {
                this.baldue -= amt;
            }
        }
    }
    
    public Card(long a) {
        this.errmsg = "";
        this.actionmsg = "";
        this.climit = 0;
        this.baldue = 0;
        this.acctno = a;
        
        try {
            BufferedReader in = new BufferedReader (new FileReader
                    ("CC" + this.acctno + ".txt"));
            this.climit = Double.parseDouble(in.readLine());
            this.baldue = Double.parseDouble(in.readLine());
            in.close();
            this.actionmsg = "Account " + this.acctno + " re-opened.";
        } catch (Exception e) {
            this.errmsg = ("Unable to re-open account #: " + a + " " 
                    + e.getMessage());
            this.acctno = 0;
            
        }
    }
    
    public ArrayList <String> getLog() {
        ArrayList<String> log = new ArrayList<>();
        this.errmsg = "";
        this.actionmsg = "";
        if (this.acctno <= 0) {
            this.errmsg = "Log requeted on unopened account.";
            return null;
        }
        try {
            BufferedReader in = new BufferedReader(
                    new FileReader("CCL" + this.acctno + ".txt"));
            String s = in.readLine();
            while (s != null) {
                log.add(s);
                s = in.readLine();
            }
            in.close();
            this.actionmsg = "Log returned for account " + this.acctno;            
        } catch (Exception e) {
            this.errmsg = "Unable to read log for account: " + this.acctno +
                    " " + e.getMessage();
        }
        return log;        
    }
    
    public void setPayment(double p) {
        this.errmsg = "";
        this.actionmsg = "";
        if (!(this.acctno > 0)) {
            this.errmsg = "Payment attempt on non-active account.";
            return;
        }
        if (p <= 0) {
            this.actionmsg = "Payment declined must be positive value.";
            writeLog(this.actionmsg);
        } else {
            //accepting payment...
            this.baldue -= p;
            if (writeStatus()) {
                this.actionmsg = "Payment of " + curr.format(p) + " posted.";
                writeLog(this.actionmsg);
            } else {
                this.baldue += p;
            }
        }
    }
    
    public void setCrIncrease(int crinc) {
        this.errmsg = "";
        this.actionmsg = "";
        if (!(this.acctno > 0)) {
            this.errmsg = "Increase requested on non-active account";
            return;
        }
        if (crinc < 49) {
            this.actionmsg = "Increased declined, increase requested must be at least $100.00";
            writeLog(this.actionmsg);
            return;
        } else {
            if ((crinc % 100) != 0) {
                //extra credit
                crinc = rounder(crinc);
                this.actionmsg = "Inital request not a multiple of 100, "
                        + "rounded to: " + curr.format(crinc);
                writeLog(this.actionmsg);
            }
            Random r = new Random();
            int x = r.nextInt(10)+1;
            if (x <= 3) {
                this.actionmsg = "Increase of " + curr.format(crinc) 
                        +" declined at this time.";
                writeLog(this.actionmsg);
            } else if (x > 4 && x < 8) {
                //adding chance of smaller increase being granted, just for fun please ignore
                crinc = crinc/2;
                this.actionmsg = "Currently, we can only offer an increase of: " 
                        + curr.format(crinc);
                writeLog(this.actionmsg);
                this.climit += crinc;
                if (writeStatus()) {
                    this.actionmsg = "Increase of " + curr.format(crinc) +
                            " is granted.";
                    writeLog(this.actionmsg);
                } else {
                    this.climit -= crinc;
                }
            } else {
                this.climit += crinc;
                if (writeStatus()) {
                    this.actionmsg = "Increase of " + curr.format(crinc) +
                            " is granted.";
                    writeLog(this.actionmsg);
                } else {
                    this.climit -= crinc; //back out bc failed status update
                }
            }
        }
        
    }
    
    public void chargeInt(double I) {
        this.errmsg = "";
        this.actionmsg = "";
        if (!(this.acctno > 0)) {
            this.errmsg = "Interest charge attempted on non-active account";
            return;
        }
        if (I <= 0) {
            this.actionmsg = "Interest charge not possible, rate must be positive";
            writeLog(this.actionmsg);
        } else {
            //accepting interest
            this.baldue *= (1 + (I/12));
            if(writeStatus()) {
                this.actionmsg = "Monthly interest charge added.";
                writeLog(this.actionmsg);
            } else {
                this.baldue /= (1 + (I/12));
            }
        }
    }
    
    private int rounder(int n) {
        
        // Smaller multiple
        int a = (n / 100) * 100;
          
        // Larger multiple
        int b = a + 100;
      
        // Return of closest of two
        return (n - a > b - n)? b : a;
    }
 
    
}//end of class
