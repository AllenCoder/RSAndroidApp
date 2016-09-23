package de.bahnhoefe.deutschlands.bahnhofsfotos;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import de.bahnhoefe.deutschlands.bahnhofsfotos.model.Bahnhof;
import de.bahnhoefe.deutschlands.bahnhofsfotos.util.BahnhofsFotoFetchTask;
import de.bahnhoefe.deutschlands.bahnhofsfotos.util.BitmapAvailableHandler;

import static android.content.Intent.createChooser;
import static android.graphics.Color.WHITE;

public class DetailsActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, BitmapAvailableHandler {
    // Names of Extras that this class reacts to
    public static final String EXTRA_TAKE_FOTO = "DetailsActivityTakeFoto";
    public static final String EXTRA_BAHNHOF = "bahnhof";
    private static final String TAG = DetailsActivity.class.getSimpleName();

    private ImageButton takePictureButton;
    private File file;
    private ImageView imageView;
    private Bahnhof bahnhof;
    private TextView tvBahnhofName;
    private boolean localFotoUsed = false;
    private static final String DEFAULT = "default";
    private String licence,photoOwner,linking,link,nickname;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static int alpha = 128;

    /**
     * Id to identify a camera permission request.
     */
    private static final int REQUEST_CAMERA = 0;
    private TextView licenseTagView;

    private BahnhofsFotoFetchTask fetchTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvBahnhofName = (TextView)findViewById(R.id.tvbahnhofname);
        imageView = (ImageView) findViewById(R.id.imageview);
        takePictureButton = (ImageButton)findViewById(R.id.button_image);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCameraPermission();
            }
        });
        enablePictureButton(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

        licenseTagView = (TextView)findViewById(R.id.license_tag);
        licenseTagView.setMovementMethod(LinkMovementMethod.getInstance());

        // switch off image and license view until we actually have a foto
        imageView.setVisibility(View.INVISIBLE);
        licenseTagView.setVisibility(View.INVISIBLE);
        takePictureButton.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        boolean directPicture = false;

        if (intent!=null) {
            bahnhof = (Bahnhof)intent.getSerializableExtra(EXTRA_BAHNHOF);
            directPicture = intent.getBooleanExtra(EXTRA_TAKE_FOTO, false);
            tvBahnhofName.setText(bahnhof.getTitle() + " (" + bahnhof.getId() + ")");

            if (bahnhof.getPhotoflag() != null) {
                fetchTask = new BahnhofsFotoFetchTask(this, createBitmapOptionsForScreen());
                fetchTask.execute(bahnhof.getId());
            } else {
                takePictureButton.setVisibility(View.VISIBLE);
                setLocalBitmap();
            }
        }
        
        // Load sharedPreferences for filling the E-Mail and variables for Filename to send
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.PREF_FILE), Context.MODE_PRIVATE);

        licence = sharedPreferences.getString(getString(R.string.LICENCE),DEFAULT);
        photoOwner = sharedPreferences.getString(getString(R.string.PHOTO_OWNER),DEFAULT);
        linking = sharedPreferences.getString(getString(R.string.LINKING),DEFAULT);
        link = sharedPreferences.getString(getString(R.string.LINK_TO_PHOTOGRAPHER),DEFAULT);
        nickname = sharedPreferences.getString(getString(R.string.NICKNAME),DEFAULT);

        if (directPicture) {
            checkCameraPermission();
        }
    }

    private void enablePictureButton(boolean enabled) {
        takePictureButton.setEnabled (enabled);
        takePictureButton.setImageAlpha(enabled ? 255 : 100);
    }

    /**
     * Method to request permission for camera
     */
    private void requestCameraPermission() {


        // Camera and Write permission has not been granted yet. Request it directly.
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CAMERA);

    }

    public void takePicture() {
        if (bahnhof.getPhotoflag() != null)
            return;
        Intent intent = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
        file = getOutputMediaFile(bahnhof.getId(), nickname);
        if (file != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            intent.putExtra(MediaStore.EXTRA_MEDIA_ALBUM, "Deutschlands Bahnhöfe");
            intent.putExtra(MediaStore.EXTRA_MEDIA_TITLE, bahnhof.getTitle());
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
        else {
            Toast.makeText(this, "Kann keine Verzeichnisstruktur anlegen", Toast.LENGTH_LONG);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CAMERA) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for camera permission.
            Log.i(TAG, "Received response for Camera permission request.");

            // Check if the only required permission has been granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                // Camera and Write permission has been granted, preview can be displayed
                enablePictureButton(true);
                takePicture();

            } else {
                //Permission not granted
                Toast.makeText(DetailsActivity.this,"You need to grant camera permission to use camera",Toast.LENGTH_LONG).show();
            }

        }
    }

    /**
     * Method to check permission
     */
    void checkCameraPermission() {
        boolean isGranted;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                // Camera permission has not been granted.

                requestCameraPermission();


            } else {

                takePicture();

            }
        }else{
            takePicture();
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // die Kamera-App sollte auf unseren internen Storage schreiben, also provozieren wir das
            // Laden des Bildes von dort
            setLocalBitmap();
        }
    }

    @NonNull
    private BitmapFactory.Options createBitmapOptionsForScreen() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        Log.d(TAG, "Screen width "+width);
        Log.d(TAG, "Screen height "+height);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.outWidth = width;
        return options;
    }

    @Nullable
    static public File getOutputMediaFile(int bahnhofNr, String nickname) {

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Bahnhofsfotos");

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                Log.e(TAG, "Cannot create directory structure "+mediaStorageDir.getAbsolutePath());
                return null;
            }
        }

        Log.d(TAG, "BahnhofNrAbfrage: " + bahnhofNr);
        File file = new File(mediaStorageDir.getPath(), String.format("%s-%d.jpg", nickname, bahnhofNr));
        Log.d("FilePfad",file.toString());


        return file;

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.details, menu);
        MenuItem navToStation = menu.findItem(R.id.nav_to_station);
        navToStation.getIcon().mutate();
        navToStation.getIcon().setColorFilter(WHITE,PorterDuff.Mode.SRC_ATOP);
        /*MenuItem takePhoto = menu.findItem(R.id.take_photo);

        takePhoto.getIcon().mutate();
        takePhoto.getIcon().setColorFilter(WHITE,PorterDuff.Mode.SRC_ATOP);*/
        

        if(localFotoUsed){

            MenuItem sendEmail = menu.findItem(R.id.send_email).setEnabled(true);
            MenuItem sharePhoto = menu.findItem(R.id.share_photo).setEnabled(true);
            sendEmail.getIcon().mutate();
            sendEmail.getIcon().setColorFilter(WHITE,PorterDuff.Mode.SRC_ATOP);
            sharePhoto.getIcon().mutate();
            sharePhoto.getIcon().setColorFilter(WHITE,PorterDuff.Mode.SRC_ATOP);

        }else{
            MenuItem sendEmail = menu.findItem(R.id.send_email).setEnabled(false);
            MenuItem sharePhoto = menu.findItem(R.id.share_photo).setEnabled(false);
            sendEmail.getIcon().mutate();
            sendEmail.getIcon().setColorFilter(WHITE,PorterDuff.Mode.SRC_ATOP);
            sendEmail.getIcon().setAlpha(alpha);
            sharePhoto.getIcon().mutate();
            sharePhoto.getIcon().setColorFilter(WHITE,PorterDuff.Mode.SRC_ATOP);
            sharePhoto.getIcon().setAlpha(alpha);
        }


        return super.onPrepareOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String navLocation = "";
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this)
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                            // Navigate up to the closest parent
                            .startActivities();
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                break;
            case R.id.nav_to_station:
                startNavigation(DetailsActivity.this);
                break;
            // action with ID action_settings was selected
            case R.id.send_email:
                Intent emailIntent = createFotoSendIntent();
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "bahnhofsfotos@deutschlands-bahnhoefe.de" });
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Bahnhofsfoto: " + bahnhof.getTitle());
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Lizenz: " + licence
                        + "\n selbst fotografiert: " + photoOwner
                        + "\n Nickname: " + nickname
                        + "\n Verlinken bitte mit: " + linking
                        + "\n Link zum Account: " + link);
                emailIntent.setType("multipart/byteranges");
                startActivity(Intent.createChooser(emailIntent,"Mail versenden"));
                break;
            case R.id.share_photo:
                Intent shareIntent = createFotoSendIntent();
                shareIntent.putExtra(Intent.EXTRA_TEXT, "#Bahnhofsfoto #dbOpendata #dbHackathon " + bahnhof.getTitle() + " @android_oma @khgdrn");
                shareIntent.setType("image/jpeg");
                startActivity(createChooser(shareIntent, "send"));
                break;
           /* case R.id.nav_to_station:

                break;*/
            default:
                break;
        }

        return true;
    }

    private Intent createFotoSendIntent() {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(DetailsActivity.this,
            "de.bahnhoefe.deutschlands.bahnhofsfotos.fileprovider", file));
        return sendIntent;
    }

    protected void startNavigation(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle(R.string.navMethod).setItems(R.array.pick_navmethod, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String dlocation = "";
                Intent intent = null;
                switch (which) {
                    case 0:
                        dlocation = String.format("google.navigation:ll=%s,%s&mode=Transit", bahnhof.getPosition().latitude, bahnhof.getPosition().longitude);
                        Log.d("findnavigation case 0", dlocation);
                        intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(dlocation));
                        break;
                    case 1:
                        dlocation = String.format("google.navigation:ll=%s,%s&mode=d", bahnhof.getPosition().latitude, bahnhof.getPosition().longitude);
                        Log.d("findnavigation case 1", dlocation);
                        intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(dlocation));
                        break;

                    case 2:
                        dlocation = String.format("google.navigation:ll=%s,%s&mode=b",
                                bahnhof.getPosition().latitude,
                                bahnhof.getPosition().longitude);
                        Log.d("findnavigation case 2", dlocation);
                        intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(dlocation));
                        break;
                    case 3:
                        dlocation = String.format("google.navigation:ll=%s,%s&mode=w", bahnhof.getPosition().latitude, bahnhof.getPosition().longitude);
                        Log.d("findnavigation case 3", dlocation);
                        intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(dlocation));
                        break;
                    case 4:
                        dlocation = String.format("geo:%s,%s?q=%s", bahnhof.getPosition().latitude, bahnhof.getPosition().longitude, bahnhof.getTitle());
                        Log.d("findnavigation case 4", dlocation);
                        intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(dlocation));
                        break;

                }
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast toast = Toast.makeText(context, R.string.activitynotfound, Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });
        builder.show();

    }


    /**
     * This gets called if the requested bitmap is available. Finish and issue the notification.
     *
     * @param publicBitmap the fetched Bitmap for the notification. May be null
     */
    @Override
    public void onBitmapAvailable(final @Nullable Bitmap publicBitmap) {
        localFotoUsed = false;
        takePictureButton.setVisibility(View.INVISIBLE);
        if (publicBitmap == null) {
            // keine Bitmap ausgelesen
            // switch off image and license view until we actually have a foto
            // @todo broken image anzeigen
            imageView.setVisibility(View.INVISIBLE);
            licenseTagView.setVisibility(View.INVISIBLE);
            return;
        }

        // Lizenzinfo aufbauen und einblenden
        licenseTagView.setVisibility(View.VISIBLE);
        if (fetchTask.getLicense() != null) {
            licenseTagView.setText(
                    String.format(
                            getText(R.string.license_tag).toString(),
                            fetchTask.getAuthor())
            );
            licenseTagView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Build an intent for an action to view the author
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW);
                            Uri authorUri = fetchTask.getAuthorReference();
                            mapIntent.setData(authorUri);
                            startActivity(mapIntent);
                        }
                    }
            );
        } else {
            licenseTagView.setText("Lizenzinfo aktuell nicht lesbar");
        }

        setBitmap(publicBitmap);
    }

    /**
     * Fetch bitmap from device local location, if  it exists, and set the foto view.
     *
     */
    private void setLocalBitmap() {
        takePictureButton.setVisibility(View.VISIBLE);

        Bitmap showBitmap = checkForLocalPhoto();
        if (showBitmap == null) {
            // lokal keine Bitmap
            localFotoUsed = false;
            // switch off image and license view until we actually have a foto
            imageView.setVisibility(View.INVISIBLE);
            licenseTagView.setVisibility(View.INVISIBLE);
            takePictureButton.setVisibility(View.VISIBLE);
            return;
        } else {
            setBitmap(showBitmap);
        }
    }


    private void setBitmap(final Bitmap showBitmap) {
        int targetWidth = createBitmapOptionsForScreen().outWidth;
        if (showBitmap.getWidth() != targetWidth) {
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(showBitmap,
                    targetWidth,
                    (int) (((long) showBitmap.getHeight() * (long) targetWidth) / showBitmap.getWidth()),
                    true);
            imageView.setImageBitmap(scaledBitmap);
        } else {
            imageView.setImageBitmap(showBitmap);
        }
        imageView.setVisibility(View.VISIBLE);
        invalidateOptionsMenu();
    }

    /**
     * Check if there's a local photo file for this station.
     * @return the Bitmap of the photo, or null if none exists.
     */
    @Nullable private Bitmap checkForLocalPhoto() {
        // show the image
        BitmapFactory.Options options = createBitmapOptionsForScreen();
        Bitmap scaledScreen = null;
        File localFile = getOutputMediaFile(bahnhof.getId(),nickname);

        if (localFile != null && localFile.canRead()) {
            Log.d(TAG, "File: "+ localFile);
            Log.d(TAG, "FileGetPath: "+ localFile.getPath().toString());

            scaledScreen = BitmapFactory.decodeFile(
                    localFile.getPath(),
                    options);
            Log.d(TAG, "img width "+scaledScreen.getWidth());
            Log.d(TAG, "img height "+scaledScreen.getHeight());
            // set license and author information
            fetchTask.setLicense(licence);
            fetchTask.setAuthorReference(null);
            localFotoUsed = true;
            return scaledScreen;
        } else {
            localFotoUsed = false;
            Log.e(TAG, "Media file not available: " + localFile.getAbsolutePath());
            return null;
        }

    }

}
