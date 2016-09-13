package SQLite;

import java.io.Serializable;

/**
 * Created by chen on 2016/9/9.
 */
public class Project implements Serializable {
    private static final long serialVersionUID = 1L;
    private String Pno;
    private String Pname;
    private String Ppassword;
    private String Pmoney;
    private String Pdate;
    private int Premember;

    public int getPremember() {
        return Premember;
    }

    public void setPremember(int premember) {
        Premember = premember;
    }

    public String getPdate() {
        return Pdate;
    }

    public void setPdate(String pdate) {
        Pdate = pdate;
    }

    public String getPno() {
        return Pno;
    }

    public void setPno(String pno) {
        Pno = pno;
    }

    public String getPname() {
        return Pname;
    }

    public void setPname(String pname) {
        Pname = pname;
    }

    public String getPpassword() {
        return Ppassword;
    }

    public void setPpassword(String ppassword) {
        Ppassword = ppassword;
    }

    public String getPmoney() {
        return Pmoney;
    }

    public void setPmoney(String pmoney) {
        Pmoney = pmoney;
    }
}
