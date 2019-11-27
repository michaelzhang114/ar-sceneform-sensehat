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
import android.net.Uri;
import android.util.Log;
import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;

import java.util.concurrent.CompletableFuture;

/**
 * Node for rendering an augmented image. The image is framed by placing the virtual picture frame
 * at the corners of the augmented image trackable.
 */
@SuppressWarnings({"AndroidApiChecker"})
public class AugmentedImageNode extends AnchorNode {

  private static final String TAG = "AugmentedImageNode";

  // The augmented image represented by this node.
  private AugmentedImage image;

  // Holds the maze model
  private Node mazeNode;
  private CompletableFuture<ModelRenderable> andyRenderable;
  private float maze_scale = 0.0f;


  public AugmentedImageNode(Context context) {
    andyRenderable = ModelRenderable.builder().setSource(context, Uri.parse("andy.sfb")).build();
  }

  /**
   * Called when the AugmentedImage is detected and should be rendered. A Sceneform node tree is
   * created based on an Anchor created from the image. The corners are then positioned based on the
   * extents of the image. There is no need to worry about world coordinates since everything is
   * relative to the center of the image, which is the parent node of the corners.
   */
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  public void setImage(AugmentedImage image) {
    this.image = image;

    // Initialize mazeNode and set its parents and the Renderable.
    // If any of the models are not loaded, process this function
    // until they all are loaded.
    if (!andyRenderable.isDone()) {
      CompletableFuture.allOf(andyRenderable)
              .thenAccept((Void aVoid) -> setImage(image))
              .exceptionally(
                      throwable -> {
                        Log.e(TAG, "Exception loading", throwable);
                        return null;
                      });
      return;
    }

    // Set the anchor based on the center of the image.
    setAnchor(image.createAnchor(image.getCenterPose()));

    mazeNode = new Node();

//    mazeNode.getScaleController().setMaxScale(0.01f);
//    mazeNode.getScaleController().setMinScale(0.01f);

    mazeNode.setParent(this);
    mazeNode.setRenderable(mazeRenderable.getNow(null));

    // scaling the maze model
    // Make sure the longest edge fits inside the image.
    final float maze_edge_size = 492.65f;
    final float max_image_edge = Math.max(image.getExtentX(), image.getExtentZ());
    maze_scale = max_image_edge / maze_edge_size;

    // Scale Y an extra 10 times to lower the maze wall.
    mazeNode.setLocalScale(new Vector3(maze_scale, maze_scale * 0.1f, maze_scale));

  }


  public void setNewImage(AugmentedImage image) {
    this.image = image;

    if (!mazeRenderable.isDone()) {
      CompletableFuture.allOf(mazeRenderable)
              .thenAccept((Void aVoid) -> setImage(image))
              .exceptionally(
                      throwable -> {
                        Log.e(TAG, "Exception loading", throwable);
                        return null;
                      });
      return;
    }


    // Set the anchor based on the center of the image.
    setAnchor(image.createAnchor(image.getCenterPose()));

    mazeNode = new Node();
    mazeNode.setParent(this);
    mazeNode.setRenderable(mazeRenderable.getNow(null));

    // scaling the maze model
    // Make sure the longest edge fits inside the image.
    final float maze_edge_size = 600f;
    final float max_image_edge = Math.max(image.getExtentX(), image.getExtentZ());
    maze_scale = max_image_edge / maze_edge_size;

    // Scale Y an extra 10 times to lower the maze wall.
    mazeNode.setLocalScale(new Vector3(maze_scale, maze_scale * 0.1f, maze_scale));
  }

  public AugmentedImage getImage() {
    return image;
  }

  public ModelRenderable getRend() {
    return mazeRenderable.getNow( null);
  }
}
