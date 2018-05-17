package com.mad18.nullpointerexception.takeabook;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mad18.nullpointerexception.takeabook.addBook.BookWrapper;

import studio.carbonylgroup.textfieldboxes.ExtendedEditText;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
public class InfoBook extends AppCompatActivity  {

    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private Animator mCurrentAnimator;

    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    private int mShortAnimationDuration;

    private int ibTextViewIds[] = new int[]{R.id.info_book_title,R.id.info_book_author, R.id.info_book_ISBN,
        R.id.info_book_editionYear,R.id.info_book_publisher, R.id.info_book_categories, R.id.info_book_description,
        R.id.info_book_pages};

    BookWrapper bookToShowInfoOf;
    private String usr_name;
    private String usr_city;
    private String usr_about;
    private FirebaseUser user;
    private Menu menu;
    private int img_not_found = 0;
    private LinearLayout horizontal_photo_list;
    private View horizontal_photo_list_element;
    private List<String> for_me;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.info_book);
        Toolbar toolbar = findViewById(R.id.info_book_toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_activity_info_book);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.title_activity_info_book);
        toolbar.setVisibility(View.VISIBLE);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bookToShowInfoOf = getIntent().getExtras().getParcelable("bookToShow");
        fillInfoBookViews();


        //simo start

//// Add 4 images

    }

    private void fillInfoBookViews() {

        TextView tv;
        tv = findViewById(R.id.info_book_title);
        tv.setText(bookToShowInfoOf.getTitle());
        tv = findViewById(R.id.info_book_author);
        String tmp;
        tmp = bookToShowInfoOf.getAuthors().toString();
        if(tmp.length()>2){
            tv.setText(tmp.substring(1,tmp.length()-1));
        }
        tv = findViewById(R.id.info_book_ISBN);
        tv.setText(bookToShowInfoOf.getISBN());

        tv = findViewById(R.id.info_book_editionYear);
        if(bookToShowInfoOf.getEditionYear() == 0){
            tv.setText(R.string.add_book_info_not_available);
        }else{
            tv.setText(Integer.toString(bookToShowInfoOf.getEditionYear()));
        }

        tv = findViewById(R.id.info_book_pages);
        if(bookToShowInfoOf.getPages() == 0){
            tv.setText(R.string.add_book_info_not_available);
        }
        else{
            tv.setText(Integer.toString(bookToShowInfoOf.getPages()));

        }

        tv = findViewById(R.id.info_book_description);
        if(bookToShowInfoOf.getDescription().length() == 0){
            tv.setText(getString(R.string.add_book_no_description));
        }else{
            tv.setText(bookToShowInfoOf.getDescription());
        }

        tv = findViewById(R.id.info_book_publisher);
        if(bookToShowInfoOf.getPublisher().length() == 0){
            tv.setText(getString(R.string.add_book_info_not_available));
        }
        else{
            tv.setText(bookToShowInfoOf.getPublisher());
        }


        tv = findViewById(R.id.info_book_categories);
        tmp = bookToShowInfoOf.getCategories().toString();
        if(tmp.length()>2){
            tv.setText(tmp.substring(1,tmp.length()-1));
        }

        ImageView iw = findViewById(R.id.info_book_main_image);
        Book book = new Book(bookToShowInfoOf);

        tv = findViewById(R.id.info_book_book_conditions);
        switch (book.getBook_condition()){

            case 0:
               tv.setText(getString(R.string.add_book_info_not_available));
                break;
            case 1:
                tv.setText(getResources().getStringArray(R.array.book_conditions)[1]);
                break;
            case 2:
                tv.setText(getResources().getStringArray(R.array.book_conditions)[2]);
                break;
            case 3:
                tv.setText(getResources().getStringArray(R.array.book_conditions)[3]);
                break;
        }
        Glide.with(this).load(book.getBook_thumbnail_url()).into(iw);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //simo inizio
        horizontal_photo_list = (LinearLayout) findViewById(R.id.info_book_list_photo_container);

        for_me = new LinkedList<>(book.getBook_photo_list().keySet());
        for (int i = 0; i < book.getBook_photo_list().size(); i++) {
            horizontal_photo_list_element = getLayoutInflater().inflate(R.layout.cell_in_image_list, null);
            ImageView imageView = (ImageView) horizontal_photo_list_element.findViewById(R.id.image_in_horizontal_list_cell);
            horizontal_photo_list.addView(horizontal_photo_list_element);
            StorageReference mImageRef = FirebaseStorage.getInstance().getReference(for_me.get(i));
            Glide.with(getApplicationContext()).load(mImageRef).into(imageView);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    zoomImageFromThumb(imageView, mImageRef);
                }
            });
        }
        //simo fine


        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        DocumentReference user_doc;

        user_doc = db.collection("users").document(bookToShowInfoOf.getUser_id());
        user_doc.get().addOnCompleteListener(task -> {
            DocumentSnapshot doc = task.getResult();
            //thisUser = doc.toObject(User.class);
            usr_name = doc.getString("usr_name");
            usr_city = doc.getString("usr_city");
            usr_about = doc.getString("usr_about");
            TextView tv2 = findViewById(R.id.info_book_owner);
            tv2.setText(usr_name);
            tv2.setTextColor(Color.BLUE);
            tv2.setClickable(true);
            tv2.setOnClickListener(view -> {
                Intent toInfoUser = new Intent(getApplicationContext() , InfoUser.class);
                toInfoUser.putExtra("usr_id", bookToShowInfoOf.getUser_id());
                toInfoUser.putExtra("usr_name",usr_name);
                toInfoUser.putExtra("usr_city", usr_city);
                toInfoUser.putExtra("usr_bio", usr_about);
                //toInfoUser.putExtra("img_uri",downloadOwnerUri);
                //qui l'immagine

                startActivity(toInfoUser);
            });

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //fillInfoBookViews();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        TextView tv;
        for(int i:ibTextViewIds){
            tv = findViewById(i);
            outState.putString(Integer.toString(i),tv.getText().toString());
        }
//        /**
//         * da mettere l'immagine thumbnail + le immagini inserite dal proprietario
//         */
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        TextView tv;
        for(int i:ibTextViewIds){
            tv = findViewById(i);
            tv.setText(savedInstanceState.getString(Integer.toString(i),""));
        }
//        /**
//         * da implementare per le immagini
//         */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void zoomImageFromThumb(final View thumbView, StorageReference mImageRef) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) findViewById(
                R.id.expanded_image);
        Glide.with(getApplicationContext()).load(mImageRef).into(expandedImageView);
        //expandedImageView.setImageResource(imageResId);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.photo_cell_container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }
}
