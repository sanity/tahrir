package tahrir.API;

/**
 * Created by Tejas Dharamsi on 6/9/2014.
 */
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
    private String author;
    private String content;
    private Date createdAt;
    private Integer id;


    public Message(String author,String content, Integer size) {
        this.author = author;
        this.content = content;
        this.createdAt = new Date();
        this.id = size;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public Integer getId() {
        return id;
    }
    public String getCreatedAt() {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        return dateFormat.format(this.createdAt);
    }

    public String getViewLink() {
        return "<a href='/article/view/" + this.id + "'>View</a>";
    }


}
