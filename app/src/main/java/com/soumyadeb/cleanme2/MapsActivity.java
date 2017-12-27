package com.soumyadeb.cleanme2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    private GoogleMap mMap;

    // Firebase Instances:
    private DatabaseReference mRootRef, mDatabase;
    private StorageReference mStorageRef;


    private ArrayList<Dustbin> dustbinList;

    // UI Instances:
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private ProgressDialog mProgress;

    // Data members:
    private String dustbinId;
    private final int CAMERA_REQ = 1000;
    private final double VISHAKHAPATNAM_LAT = 17.6868;
    private final double VISHAKHAPATNAM_LONG = 83.2185;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        // Initialize UI instances:
        fab = (FloatingActionButton)findViewById(R.id.fab);


        // Initialize firebase instances:
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mProgress = new ProgressDialog(this);

        dustbinList = new ArrayList<>();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mDatabase = mRootRef.child("dustbins").child("GVMC");

        // Retrieve list of dustbins stored in the database:
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String id = dataSnapshot.getKey().toString();
                String latitude = dataSnapshot.child("latitude").getValue().toString();
                String longitude = dataSnapshot.child("longitude").getValue().toString();
                String city = dataSnapshot.child("city").getValue().toString();
                String locality = dataSnapshot.child("locality").getValue().toString();
                String last_clean = dataSnapshot.child("last_clean").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String municipality = dataSnapshot.child("municipality").getValue().toString();

                Dustbin dustbin = new Dustbin(id, latitude, longitude, city, locality, last_clean, status, municipality);
                dustbinList.add(dustbin);
                int MARKER_RESOURCE=R.drawable.ic_marker_dustbin_clean;
                if(status.equals("clean")){
                    MARKER_RESOURCE = R.drawable.ic_marker_dustbin_clean;
                }
                else {
                    MARKER_RESOURCE = R.drawable.ic_marker_dustbin_full;
                }
                LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                mMap.addMarker(new MarkerOptions().position(latLng).title("Dustbin ID: "+id)
                        .icon(Tools.bitmapDescriptorFromVector(MapsActivity.this, MARKER_RESOURCE)));


                Log.d("asdf", ""+dataSnapshot);
                Log.d("asdf", "id :"+id);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // Leave empty
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // Leave empty
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                // Leave empty
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Leave empty
            }
        });



        // OnClick handler for Floating Action Button:
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setPadding(0,150,0,0);

        LatLng latLng = new LatLng(VISHAKHAPATNAM_LAT, VISHAKHAPATNAM_LONG);
        mMap.addMarker(new MarkerOptions().position(latLng).title("Current location"));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 11);
        mMap.animateCamera(cameraUpdate);

    }


    // Method to scan QR code:
    private void startScan() {
        IntentIntegrator integrator = new IntentIntegrator(MapsActivity.this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setBeepEnabled(true);
        integrator.setCameraId(0);
        integrator.setPrompt("Scan QR Code");
        integrator.setBarcodeImageEnabled(false);
        integrator.setCaptureActivity(CaptureActivityPortrait.class);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_REQ){
            if (resultCode == RESULT_OK) {
                Bundle bundle=data.getExtras();
                Bitmap image=(Bitmap)bundle.get("data");
                mProgress.setMessage("Uploading data...");
                mProgress.show();
                uploadData(image, dustbinId);
            }
        }

        // Handler for QR Scanner activity:
        else {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() != null) {
                    Toast.makeText(this, "Scan result: " + result.getContents(), Toast.LENGTH_LONG).show();
                    dustbinId = result.getContents();

                    if(TextUtils.isEmpty(dustbinId)){
                        Toast.makeText(this, "Invalid dustbin ID", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Intent camera=new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);  //android.provider gives full access to camera software
                        startActivityForResult(camera,CAMERA_REQ);
                    }

                } else {
                    Toast.makeText(this, "Scan cancelled.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    // Method to upload data to Firebase storage:
    private void uploadData(final Bitmap image, final String dustbinId) {

        // Check for invalid values:
        if(image == null || dustbinId == null){
            Toast.makeText(MapsActivity.this, "Invalid data, please try again", Toast.LENGTH_LONG).show();
            mProgress.dismiss();
        }

        // Initialize upload if data is valid:
        else {

            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Check of dustbin ID is present in the database:
                    if(dataSnapshot.hasChild(dustbinId)){
                        StorageReference mStoreImage = mStorageRef.child("images/municipality/GVMC/"+Tools.random()+".jpg");

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] data = baos.toByteArray();

                        UploadTask uploadTask = mStoreImage.putBytes(data);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                mProgress.dismiss();
                                Toast.makeText(MapsActivity.this, "Upload failed. Please try again.", Toast.LENGTH_LONG).show();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                DatabaseReference mFullDustbinsRef = mRootRef.child("full_dustbins/GVMC");
                                Map<String, String> fullDustbinMap = new HashMap();
                                fullDustbinMap.put("image", downloadUrl.toString());
                                Calendar calendar = Calendar.getInstance();
                                fullDustbinMap.put("timestamp", String.valueOf(calendar.getTimeInMillis()));
                                fullDustbinMap.put("dustbin_id",dustbinId);

                                // Update dustbin database:
                                mFullDustbinsRef.child(dustbinId).setValue(fullDustbinMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Change status of the dustbin to 'full':
                                        mDatabase.child(dustbinId).child("status").setValue("full");
                                        mProgress.dismiss();
                                        startActivity(new Intent(MapsActivity.this, ResponseActivity.class));
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        mProgress.dismiss();
                                        Toast.makeText(MapsActivity.this, "Upload failed. Please try again.", Toast.LENGTH_LONG).show();
                                    }
                                });

                            }
                        });
                    }
                    // Handle case when Dustbin ID is not present in database:
                    else {
                        mProgress.dismiss();
                        Toast.makeText(MapsActivity.this, "Invalid dustbin ID. Please try again.", Toast.LENGTH_LONG).show();

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }
    }


}
