package io.github.kiryu1223.baseDao.test;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class User
{
    private Integer id;
    private String userName;
    private String passWord;
    private Integer role;
    private String realName;
    private String phone;
    private String address;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    @Basic
    @Column(name = "user_name", nullable = true, length = 255)
    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    @Basic
    @Column(name = "pass_word", nullable = true, length = 255)
    public String getPassWord()
    {
        return passWord;
    }

    public void setPassWord(String passWord)
    {
        this.passWord = passWord;
    }

    @Basic
    @Column(name = "role", nullable = true)
    public Integer getRole()
    {
        return role;
    }

    public void setRole(Integer role)
    {
        this.role = role;
    }

    @Basic
    @Column(name = "real_name", nullable = true, length = 255)
    public String getRealName()
    {
        return realName;
    }

    public void setRealName(String realName)
    {
        this.realName = realName;
    }

    @Basic
    @Column(name = "phone", nullable = true, length = 255)
    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    @Basic
    @Column(name = "address", nullable = true, length = 255)
    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(userName, user.userName) && Objects.equals(passWord, user.passWord) && Objects.equals(role, user.role) && Objects.equals(realName, user.realName) && Objects.equals(phone, user.phone) && Objects.equals(address, user.address);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, userName, passWord, role, realName, phone, address);
    }
}
