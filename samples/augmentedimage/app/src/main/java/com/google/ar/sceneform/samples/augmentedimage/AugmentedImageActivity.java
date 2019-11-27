/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ar.sceneform.samples.augmentedimage;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.samples.common.helpers.SnackbarHelper;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseTransformableNode;
import com.google.ar.sceneform.ux.TransformableNode;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * This application demonstrates using augmented images to place anchor nodes. app to include image
 * tracking functionality.
 *
 * <p>In this example, we assume all images are static or moving slowly with a large occupation of
 * the screen. If the target is actively moving, we recommend to check
 * ArAugmentedImage_getTrackingMethod() and render only when the tracking method equals to
 * AR_AUGMENTED_IMAGE_TRACKING_METHOD_FULL_TRACKING. See details in <a
 * href="https://developers.google.com/ar/develop/c/augmented-images/">Recognize and Augment
 * Images</a>.
 */
public class AugmentedImageActivity extends AppCompatActivity {

    private int b = 0;
    private ArFragment arFragment;
    private ImageView fitToScanView;
    private ModelRenderable andyRenderable;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private SensorDataManager sdm;
    private Material mat = null;
    private String[] output;
    private int frHackNum = 0;

    // Augmented image and its associated center pose anchor, keyed by the augmented image in
    // the database.
    private final Map<AugmentedImage, AugmentedImageNode> augmentedImageMap = new HashMap<>();

    /**
     * Gets sensor data by calling the flask API
     */
    private String[] getSensorData() {
        // Get a RequestQueue
        RequestQueue queue = MySingleton.getInstance(this.getApplicationContext()).
                getRequestQueue();
        // make request
        //String url = "http://10.197.119.190:5000/";
        String url = "http://192.168.0.42:5000/";
        JsonArrayRequest jsonRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Log.e("rest response!!", response.toString());
                        output = response.toString().split(",");
                        //String[] data = response.toString().split(",");
                        // TODO: do something with this data
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        throw new ServerErrorException();
                    }
                }
        );

        // Add a request (in this example, called stringRequest) to your RequestQueue.
        MySingleton.getInstance(this).addToRequestQueue(jsonRequest);
        return output;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sdm = new SensorDataManager();
//    sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//    accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        setContentView(R.layout.activity_main);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        fitToScanView = findViewById(R.id.image_view_fit_to_scan);

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        if (augmentedImageMap.isEmpty()) {
            fitToScanView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Registered with the Sceneform Scene object, this method is called at the start of each frame.
     *
     * @param frameTime - time since last frame.
     */
    private void onUpdateFrame(FrameTime frameTime) {
        frHackNum++;

        if (frHackNum % 15 == 0) {
            sdm.addNewDataPoint(getSensorData());
        }

        Frame frame = arFragment.getArSceneView().getArFrame();

        // If there is no frame, just return.
        if (frame == null) {
            return;
        }

        Collection<AugmentedImage> updatedAugmentedImages =
                frame.getUpdatedTrackables(AugmentedImage.class);


        if (sdm.isShaking()) {
            Log.e("shaking now!", Integer.toString(b));
        }

        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            switch (augmentedImage.getTrackingState()) {
                case PAUSED:
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                    // but not yet tracked.
                    String text = "Detected Image " + augmentedImage.getIndex();
                    SnackbarHelper.getInstance().showMessage(this, text);
                    break;

                case TRACKING:
                    // Have to switch to UI Thread to update View.
                    fitToScanView.setVisibility(View.GONE);

                    // Create a new anchor for newly found images.
                    if (!augmentedImageMap.containsKey(augmentedImage)) {
                        Log.e("new anchor", Integer.toString(b));

                        AugmentedImageNode node = new AugmentedImageNode(this);
                        node.setImage(augmentedImage);
                        augmentedImageMap.put(augmentedImage, node);
                        arFragment.getArSceneView().getScene().addChild(node);
                    }
                    if (sdm.pitchUp() && augmentedImageMap.get(augmentedImage).getAnchor() != null) {
//                        AugmentedImageNode n = augmentedImageMap.get(augmentedImage);
//                        arFragment.getArSceneView().getScene().removeChild(n);
//                        n.getAnchor().detach();
//                        n.setParent(null);
//                        n = null;
//                        augmentedImageMap.remove(augmentedImage);

                        AugmentedImageNode node = augmentedImageMap.get(augmentedImage);
                        //BaseTransformableNode tn = (BaseTransformableNode) augmentedImageMap.get(augmentedImage);

                        //node.setLocalRotation(Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 50.0f));

                        ModelRenderable rend = node.getRend();
                        if (rend == null) break;
                        mat = rend.getMaterial().makeCopy();
                        if (mat != null) {
                            mat.setFloat3("baseColorTint", 200,0,0);
                            rend.setMaterial(mat);
                        }
                    }
                    else if (! sdm.pitchUp() && mat != null) {
                        Log.e("stopped shaking!", Integer.toString(b));
                        AugmentedImageNode node = augmentedImageMap.get(augmentedImage);
                        //node.setLocalRotation(Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), -50.0f));

                        ModelRenderable rend = node.getRend();
                        if (rend == null) break;
                        mat = rend.getMaterial().makeCopy();
                        if (mat != null) {
                            mat.setFloat3("baseColorTint", 0,0,200);
                            rend.setMaterial(mat);
                        }
                    }
                    break;

                case STOPPED:
                    augmentedImageMap.remove(augmentedImage);
                    break;
            }
        }
    }
}
