package SQLite;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

/**
 * Created by chen on 2016/9/7.
 */
public class Money implements Serializable {
    private static final long serialVersionUID = 1L;
    private int Mno;
    private String Mreason;
    private String Money;
    private String Mdate;
    private byte[] Mimage;
    private byte[]  Maccept;
    private String Pno;
    private String Ppassword;

    public String getPpassword() {
        return Ppassword;
    }

    public void setPpassword(String ppassword) {
        Ppassword = ppassword;
    }

    public String getPno() {
        return Pno;
    }

    public void setPno(String pno) {
        Pno = pno;
    }

    public int getMno() {
        return Mno;
    }

    public void setMno(int mno) {
        Mno = mno;
    }

    public String getMreason() {
        return Mreason;
    }

    public void setMreason(String mreason) {
        Mreason = mreason;
    }

    public String getMoney() {
        return Money;
    }

    public void setMoney(String money) {
        Money = money;
    }

    public String getMdate() {
        return Mdate;
    }

    public void setMdate(String mdate) {
        Mdate = mdate;
    }

    public byte[] getMimage() {
        return Mimage;
    }

    public void setMimage(byte[] mimage) {
        Mimage = mimage;
    }

    public byte[] getMaccept() {
        return Maccept;
    }

    public void setMaccept(byte[] maccept) {
        Maccept = maccept;
    }
}
