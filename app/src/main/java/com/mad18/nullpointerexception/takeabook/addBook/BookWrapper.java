package com.mad18.nullpointerexception.takeabook.addBook;

import android.os.Parcel;
import android.os.Parcelable;

import com.mad18.nullpointerexception.takeabook.Book;

import java.util.LinkedList;
import java.util.List;

/**
 * La classe BookWrapper si occupa di creare un oggetto in cui possano essere memorizzati i dati
 * relativi ad un libro. La classe implementa l'interfaccia Parcelable in quanto è necessario creare
 * un oggetto di questo tipo affinchè possa essere inserito nel bundle.
 *
 */
public class BookWrapper extends Book implements Parcelable {

    /**
     * Si occupa di creare una nuova istanza della classe Parcelable, instanziandola dal Parcel specificato.
     * Il parcel deve essere precedentemente scritto con il metodo WriteToParcel
     */
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public BookWrapper createFromParcel(Parcel in) {
            return new BookWrapper(in);
        }
        public BookWrapper[] newArray(int size) {
            return new BookWrapper[size];
        }
    };
    private String bookwrapper_ISBN;
    private String bookwrapper_title;
    //first author qui assente
    private String bookwrapper_publisher;
    private String bookwrapper_thumbnail_url;
    private int bookwrapper_editionYear;
    //book_condition qui assente
    private String bookwrapper_user_id;
    // private Map<String,Boolean> bookwrapper_authors;
    private List<String> bookwrapper_authors; //in book map
    private List<String> bookwrapper_categories; // in book map
    private String bookwrapper_description;
    private double bookwrapper_lat, bookwrapper_longitude; //in book abbiamo geopoint


    private List<String> bookwrapper_photos_of_book_from_user_url = new LinkedList<>();

    //Constructor a partire dai campi inseriti per la creazione di bookwrapper senza partire da un Book
    public BookWrapper(String bookwrapper_ISBN, String bookwrapper_title, List<String> bookwrapper_authors, String bookwrapper_publisher, int bookwrapper_editionYear, String bookwrapper_thumbnail_url, List<String> bookwrapper_categories, String bookwrapper_description, double bookwrapper_lat, double bookwrapper_longitude){
        this.bookwrapper_ISBN = bookwrapper_ISBN;
        this.bookwrapper_title = bookwrapper_title;
        this.bookwrapper_authors = bookwrapper_authors;
        this.bookwrapper_publisher = bookwrapper_publisher;
        this.bookwrapper_editionYear = bookwrapper_editionYear;
        this.bookwrapper_thumbnail_url = bookwrapper_thumbnail_url;
        this.bookwrapper_categories = bookwrapper_categories;
        this.bookwrapper_description = bookwrapper_description;
        this.bookwrapper_user_id = "";
        this.bookwrapper_lat = bookwrapper_lat;
        this.bookwrapper_longitude = bookwrapper_longitude;
    }

    //Costruttore per creare un Bookwrapper a partire da un Book già esistente
    public BookWrapper(Book book){
        this.bookwrapper_ISBN = book.getBook_ISBN();
        this.bookwrapper_title = book.getBook_title();
        this.bookwrapper_authors = new LinkedList<>(book.getBook_authors().keySet());
        this.bookwrapper_publisher = book.getBook_publisher();
        this.bookwrapper_editionYear = book.getBook_editionYear();
        this.bookwrapper_thumbnail_url = book.getBook_thumbnail_url();
        this.bookwrapper_categories = new LinkedList<>(book.getBook_categories().keySet());
        this.bookwrapper_description = book.getBook_description();
        this.bookwrapper_user_id = book.getBook_userid();
        this.bookwrapper_lat = book.getBook_location().getLatitude();
        this.bookwrapper_longitude = book.getBook_location().getLongitude();
    }

    public List<String> getPhotos_of_book_from_user_url() {
        return bookwrapper_photos_of_book_from_user_url;
    }

    public void setPhotos_of_book_from_user_url(List<String> photos_of_book_from_user_url) {
        this.bookwrapper_photos_of_book_from_user_url = photos_of_book_from_user_url;
    }

    public String getBookwrapper_description() {
        return bookwrapper_description;
    }

    public void setBookwrapper_description(String bookwrapper_description) {
        this.bookwrapper_description = bookwrapper_description;
    }

    public String getBookwrapper_ISBN() {
        return bookwrapper_ISBN;
    }

    public void setBookwrapper_ISBN(String bookwrapper_ISBN) {
        this.bookwrapper_ISBN = bookwrapper_ISBN;
    }

    public String getBookwrapper_title() {
        return bookwrapper_title;
    }

    public void setBookwrapper_title(String bookwrapper_title) {
        this.bookwrapper_title = bookwrapper_title;
    }

    public List<String> getBookwrapper_authors() {
        return bookwrapper_authors;
    }

    public void setBookwrapper_authors(List<String> bookwrapper_authors) {
        this.bookwrapper_authors = bookwrapper_authors;
    }

    public String getBookwrapper_publisher() {
        return bookwrapper_publisher;
    }

    public void setBookwrapper_publisher(String bookwrapper_publisher) {
        this.bookwrapper_publisher = bookwrapper_publisher;
    }

    public int getBookwrapper_editionYear() {
        return bookwrapper_editionYear;
    }

    public void setBookwrapper_editionYear(int bookwrapper_editionYear) {
        this.bookwrapper_editionYear = bookwrapper_editionYear;
    }

    public String getBookwrapper_thumbnail_url() {
        return bookwrapper_thumbnail_url;
    }

    public void setBookwrapper_thumbnail_url(String bookwrapper_thumbnail_url) {
        this.bookwrapper_thumbnail_url = bookwrapper_thumbnail_url;
    }

    public List<String> getBookwrapper_categories() {
        return bookwrapper_categories;
    }

    public void setBookwrapper_categories(List<String> bookwrapper_categories) {
        this.bookwrapper_categories = bookwrapper_categories;
    }

    public String getBookwrapper_user_id() {
        return bookwrapper_user_id;
    }

    public double getBookwrapper_lat() {
        return bookwrapper_lat;
    }

    public void setBookwrapper_lat(double bookwrapper_lat) {
        this.bookwrapper_lat = bookwrapper_lat;
    }

    public double getBookwrapper_longitude() {
        return bookwrapper_longitude;
    }

    public void setBookwrapper_longitude(double bookwrapper_longitude) {
        this.bookwrapper_longitude = bookwrapper_longitude;
    }

    public void setBookwrapper_user_id(String bookwrapper_user_id) {
        this.bookwrapper_user_id = bookwrapper_user_id;
    }

    public BookWrapper(Parcel in){
        this.bookwrapper_ISBN = in.readString();
        this.bookwrapper_title = in.readString();
        this.bookwrapper_authors = in.createStringArrayList();
        this.bookwrapper_publisher = in.readString();
        this.bookwrapper_editionYear = in.readInt();
        this.bookwrapper_thumbnail_url = in.readString();
        this.bookwrapper_categories = in.createStringArrayList();
        this.bookwrapper_description = in.readString();
        this.bookwrapper_user_id = in.readString();
        this.bookwrapper_longitude = in.readDouble();
        this.bookwrapper_lat = in.readDouble();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Questo metodo si occupa di scrivere nel Parcel di destinazione i dati richiesti.
     * @param dest
     * @param flags
     */

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.bookwrapper_ISBN);
        dest.writeString(this.bookwrapper_title);
        //dest.writeMap(this.bookwrapper_authors);
        dest.writeStringList(this.bookwrapper_authors);
        dest.writeString(this.bookwrapper_publisher);
        dest.writeInt(this.bookwrapper_editionYear);
        dest.writeString(this.bookwrapper_thumbnail_url);
        dest.writeStringList(this.bookwrapper_categories);
        dest.writeString(this.bookwrapper_description);
        dest.writeString(this.bookwrapper_user_id);
        dest.writeDouble(this.bookwrapper_lat);
        dest.writeDouble(this.bookwrapper_longitude);
    }
    @Override
    public String toString() {
        String Totauthors= ", bookwrapper_authors='";
//        for (String key : bookwrapper_authors.keySet()) {
//            Totauthors = Totauthors + key + '\'';
//        }
        Totauthors = Totauthors +this.bookwrapper_authors.toString()+'\'';

        String SeditionYear = Integer.toString(bookwrapper_editionYear);

        return "BookWrapper{"+"bookwrapper_ISBN='"+ bookwrapper_ISBN +'\''+
                ", bookwrapper_title='" + bookwrapper_title + '\'' +
                Totauthors +
                ", bookwrapper_publisher='" + bookwrapper_publisher + '\'' +
                ", bookwrapper_editionYear='" + SeditionYear + '\'' +
                ", bookwrapper_description='" + bookwrapper_description + '\'' +
                ", bookwrapper_user_id='" + bookwrapper_user_id + '\'' +
                '}';
    }
}