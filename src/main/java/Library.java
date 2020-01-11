
import java.sql.*;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import java.util.Scanner;

public class Library {

    public static void main(String[] args) throws SQLException {


        //第一种连接数据库的方法
//        Class.forName("com.mysql.jdbc.Driver");
//        //通过DriverManager连接数据库
//        java.sql.Connection connection = DriverManager.getConnection(
//                "jdbc:mysql://localhost:3306/library?useSSL=false&characterEncoding=utf8",
//                "root",
//                "wx123456"
//        );
//        Statement statement=connection.createStatement();

        //第二种  不带连接池的
        // DataSource dataSource =new MysqlDataSource();
        // 带连接池的
        MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
        dataSource.setServerName("127.0.0.1");
        dataSource.setPort(3306);
        dataSource.setDatabaseName("library");
        dataSource.setUser("root");
        dataSource.setPassword("wx123456");
        dataSource.setUseSSL(false);
        dataSource.setCharacterEncoding("utf8");
        Connection connection = dataSource.getConnection();

        Scanner in = new Scanner(System.in);
        int flag = 0;//选项
        while (flag != -1) {
            menu();
            flag = in.nextInt();
            switch (flag) {
                case 1:
                    add(in, connection);//添加
                    break;
                case 2:
                    alter( in,connection);//修改
                    break;
                case 3:
                    delect(in, connection);//删除
                    break;
                case 4:
                    query(in, connection);//查询
                    break;
                case 5:
                    borrow(in, connection);//借阅
                    break;
                case 6:
                    Bquery(in,connection);//借阅信息查询
                    break;
                case 7:
                    back(in, connection); //归还
                    break;
                case -1:
                    flag = -1;
                    break;
                default:
                    System.out.println("输入有误，重新选择");
                    break;

            }
        }
    }
    //归还
    private static void back(Scanner in, Connection connection) {
        System.out.println("输入归还--书的编号");
        String bookID = in.next();

        //从数据表Borrow中删除借阅记录
        {
            String sql = "select *from Borrow where BID=? ";
            try (PreparedStatement statement = connection.prepareStatement(sql)){

                statement.setString(1, bookID);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet==null) {
                    System.out.println("这本书没有被借阅过");
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            String sql1 = "delete from Borrow where BID=?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                PreparedStatement statement1 = connection.prepareStatement(sql1);
                statement1.setString(1, bookID);
                statement1.executeUpdate();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        //从书的表中将状态改为true
        {
            String sql = "update books set Isstate=?  where BID=? ";
            try(PreparedStatement statement = connection.prepareStatement(sql)){
                    String sql1 = "update books set Isstate=?  where BID=? ";
                    statement.setString(1, "T");
                    statement.setString(2,bookID);
                    statement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }


        }
        System.out.println("归还成功");


    }
    //借阅信息查询
    private static void Bquery(Scanner in, Connection connection) {
        System.out.println("->请输入你的编号");
        String userID=in.next();
        String sql="select *from Borrow where SID =?";
        try(PreparedStatement statement = connection.prepareStatement(sql)){

            statement.setString(1,userID);
            ResultSet resultSet=statement.executeQuery();
            while(resultSet.next()){
                String Bname = resultSet.getString(1);
                String BID = resultSet.getString(2);
                String Sname = resultSet.getString(3);
                String SID = resultSet.getString(4);
                System.out.println(
                        ", Bname='" + Bname + '\'' +
                        ", BID=" + BID +
                        ", Sname='" + Sname + '\'' +
                        ", SID='" + SID + '\'');

            }
        } catch (SQLException e) {

            e.printStackTrace();
        }
    }
    //借阅
    private static void borrow(Scanner in, Connection connection) {
        System.out.println("输入你的编号");//相当于登录
        String userId=in.next();
        String Bname;
        String BID;
        String Sname=null;
        String url="select *from borrower where SID=?";
        try (PreparedStatement statement = connection.prepareStatement(url)){

            statement.setString(1,userId);
            ResultSet resultSet=statement.executeQuery();
                if(resultSet.next()==false){
                    System.out.println("你没有借书权限");
                    return;
                }
                if(resultSet.first()){
                    Sname=resultSet.getString(1);
                }
            System.out.println(Sname+"欢迎借书");
        } catch (SQLException e) {
            e.printStackTrace();
        }


        System.out.println("输入要借阅的书名");
        Bname = in.next();

        String sql = "select * from books where Bname=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)){

            statement.setString(1, Bname);
            ResultSet resultSet=statement.executeQuery();
            while(resultSet.next()){
                String isState =resultSet.getString(8);
                if(isState.equals("T")){
                    BID = resultSet.getString(2);
                    String author = resultSet.getString(3);
                    String press = resultSet.getString(4);
                    String Btype = resultSet.getString(5);
                    String location = resultSet.getString(6);
                    String SBlocation =resultSet.getString(7);

                    System.out.println("name='" + Bname + '\'' +
                            ", BID='" + BID + '\'' +
                            ", author='" + author + '\'' +
                            ", press=" + press +
                            ", Btype='" + Btype + '\'' +
                            ", location='" + location + '\''+
                            ", SBlocation=" + SBlocation );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("以上是名字为"+Bname+"图书");
        System.out.println("输入你选择OK--书的编号");
        String bookID=in.next();
        String sql1="update books "
                  +"set Isstate=? "+
                "where BID=?";
 //       String  sql1="update books set Isstate ='F' where BID='9787302457787'";

        String sql2="insert into Borrow values(?,?,?,?)";
        try(PreparedStatement statement = connection.prepareStatement(sql1)){
        statement.setString(1,"F");
        statement.setString(2,bookID );
        statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try(PreparedStatement statement1 = connection.prepareStatement(sql2)){
        statement1.setString(1,Bname);
        statement1.setString(2,bookID );
        statement1.setString(3,Sname);
        statement1.setString(4,userId);
        statement1.executeUpdate();

    } catch (SQLException e) {
        e.printStackTrace();
    }
        System.out.println("借阅成功");
    }
    //查询
    private static void query(Scanner in, Connection connection) {
        System.out.println("->输入要查找的书名");
        String Bname=in.next();
        String sql="select * from books where Bname=? order by BID";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, Bname);
            ResultSet resultSet=statement.executeQuery();
            while(resultSet.next()){

                String BID = resultSet.getString(2);
                String author = resultSet.getString(3);
                String press = resultSet.getString(4);
                String Btype = resultSet.getString(5);
                String location = resultSet.getString(6);
                String SBlocation =resultSet.getString(7);
                String Isstate =resultSet.getString(8);

                System.out.println("name='" + Bname + '\'' +
                        ", BID='" + BID + '\'' +
                        ", author='" + author + '\'' +
                        ", press=" + press +
                        ", Btype='" + Btype + '\'' +
                        ", location='" + location + '\''+
                        ", SBlocation=" + SBlocation +
                        ", Isstate='" + Isstate + '\'' );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }
    //删除
    private static void delect(Scanner in, Connection connection) {
        System.out.println("->输入要删除的编号");
        String BID=in.next();
        String sql="delete from books where BID=?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, BID);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    //修改
    private static void alter(Scanner in,Connection connection) {
        System.out.println("->请输入要修改书信息的书编号");
        String BID=in.next();

        System.out.println("->请输入修改后的书名");
        String Bname = in.next();
        System.out.println("->请输入修改后的作者");
        String author = in.next();
        System.out.println("->请输入修改后的出版社");
        String press = in.next();
        System.out.println("->请输入修改后的书的类型");
        String Btype = in.next();
        System.out.println("->请输入修改后的存放位置");
        String location = in.next();
        System.out.println("->请输入修改后的同名书的存放位置");
        String SBlocation = in.next();

        String sql ="update books " +
                "set Bname=?,author=?,press=?,Btype=?,location=?,SBlocation=?" +
                "where BiD=?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, Bname);
            statement.setString(2, author);
            statement.setString(3, press);
            statement.setString(4, Btype);
            statement.setString(5, location);
            statement.setString(6, SBlocation);
            statement.setString(7, BID);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //添加书籍
    private static void add(Scanner in, Connection connection) {
        System.out.println("->请输入书名");
        String Bname = in.next();
        System.out.println("->请输入编号");
        String BID = in.next();
        System.out.println("->请输入作者");
        String author = in.next();
        System.out.println("->请输入出版社");
        String press = in.next();
        System.out.println("->请输入书的类型");
        String Btype = in.next();
        System.out.println("->请输入存放位置");
        String location = in.next();
        System.out.println("->请输入同名书的存放位置");
        String SBlocation = in.next();
        String Isstate ="T";

        String sql = "insert into books values (?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, Bname);
            statement.setString(2, BID);
            statement.setString(3, author);
            statement.setString(4, press);
            statement.setString(5, Btype);
            statement.setString(6, location);
            statement.setString(7, SBlocation);
            statement.setString(8, Isstate);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //菜单
    private static void menu() {
        System.out.println("选择菜单");
        System.out.println("1->添加");
        System.out.println("2->修改");
        System.out.println("3->删除");
        System.out.println("4->查询");
        System.out.println("5->借阅");
        System.out.println("6->借阅信息查询");
        System.out.println("7->归还");
        System.out.println(" ->请选择");
    }
}