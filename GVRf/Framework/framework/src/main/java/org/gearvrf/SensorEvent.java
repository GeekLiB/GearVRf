/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf;

import android.util.Log;

import org.joml.Vector3f;

/**
 * This class provides all the information corresponding to events generated by
 * a {@link GVRBaseSensor}.
 * 
 * When GVRf detects cases where a given {@link GVRBaseSensor} is activated by
 * user interaction the {@link ISensorEvents} is triggered and a
 * {@link SensorEvent} is delivered to the application.
 * 
 * Make sure that the app consumes the {@link SensorEvent} data within the
 * callback as these objects are recycled.
 *
 */
public class SensorEvent {
    private static final String TAG = SensorEvent.class.getSimpleName();
    private boolean isActive;
    private boolean isOver;
    private GVRSceneObject object;
    private GVRCursorController controller;

    // We take a leaf out of the MotionEvent book to implement linked
    // recycling of objects.
    private static final int MAX_RECYCLED = 10;
    private static final Object recyclerLock = new Object();

    private static int recyclerUsed;
    private static SensorEvent recyclerTop;
    private SensorEvent next;
    private Vector3f hitPoint;

    SensorEvent(){
        hitPoint = new Vector3f();
    }

    /**
     * Set the active flag on the {@link SensorEvent}
     * 
     * @param isActive
     *            The active flag value.
     */
    void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * 
     * Set the {@link GVRCursorController} on the {@link SensorEvent}
     * 
     * @param controller
     *            The {@link GVRCursorController} that created this event
     */
    void setCursorController(GVRCursorController controller) {
        this.controller = controller;
    }

    /**
     * Set the coordinates of the intersection between the input ray and the
     * affected object with the {@link GVRBaseSensor}.
     *
     * @param hitX X co-ordinate of the hit point
     * @param hitY Y co-ordinate of the hit point
     * @param hitZ Z co-ordinate of the hit point
     */
    void setHitPoint(float hitX, float hitY, float hitZ) {
        hitPoint.set(hitX, hitY, hitZ);
    }

    /**
     * The {@link GVRSceneObject} that triggered this {@link SensorEvent}.
     * 
     * @param object
     *            The affected object.
     */
    void setObject(GVRSceneObject object) {
        this.object = object;
    }

    /**
     * This flag denotes that the {@link GVRCursorController} "is over" the
     * affected object.
     * 
     * @param isOver
     *            The value of the "is over" flag.
     */
    void setOver(boolean isOver) {
        this.isOver = isOver;
    }

    /**
     * Use this call to retrieve the affected object.
     * 
     * @return The affected {@link GVRSceneObject} that caused this
     *         {@link SensorEvent} to be triggered.
     */
    public GVRSceneObject getObject() {
        return object;
    }

    /**
     * Use this flag to detect if the input "is over" the {@link GVRSceneObject}
     * .
     * 
     * @return <code>true</code> if the input is over the corresponding
     *         {@link GVRSceneObject}. The {@link ISensorEvents} delivers
     *         multiple sensor events when this state is <code>true</code> and
     *         only one event when this state is <code>false</code>.
     * 
     */
    public boolean isOver() {
        return isOver;
    }

    /**
     * Get the X component of the hit point.
     *
     * The values returned by this call persist only for the duration of the
     * {@link ISensorEvents#onSensorEvent(SensorEvent)} call. Make sure to make a copy of this
     * value if you wish to use it past its lifetime.
     *
     * @return 'X' component of the hit point.
     */
    public float getHitX() {
        return hitPoint.x;
    }

    /**
     * Get the 'Y' component of the hit point.
     *
     * The values returned by this call persist only for the duration of the
     * {@link ISensorEvents#onSensorEvent(SensorEvent)} call. Make sure to make a copy of this
     * value if you wish to use it past its lifetime.
     *
     * @return 'Y' component of the hit point.
     */
    public float getHitY() {
        return hitPoint.y;
    }

    /**
     * Get the 'Z' component of the hit point.
     * The values returned by this call persist only for the duration of the
     * {@link ISensorEvents#onSensorEvent(SensorEvent)} call. Make sure to make a copy of this
     * value if you wish to use it past its lifetime.
     *
     * @return 'Z' component of the hit point.
     */
    public float getHitZ() {
        return hitPoint.z;
    }

    /**
     * Returns the hit point of the input and the affected
     * {@link GVRSceneObject}
     *
     * @return The coordinates where the input intersects with the
     * {@link GVRSceneObject}.
     * @deprecated Returns a new float array every call which may lead to
     * frequent GC cycles. Use the more efficient call to {{@link #getHitX()}},
     * {{@link #getHitY()}} and {{@link #getHitZ()}}.
     */
    public float[] getHitPoint() {
        return new float[]{hitPoint.x, hitPoint.y, hitPoint.z};
    }

    /**
     * Returns the active status of the {@link SensorEvent}.
     * 
     * @return <code>true</code> when the provided input has an active state and
     *         <code>false</code> otherwise.
     * 
     *         This usually denotes a button press on a given input event. The
     *         actual interaction that causes the active state is defined by the
     *         input provided to the {@link GVRInputManagerImpl}.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Returns the {@link GVRCursorController} that generated this event.
     * 
     * @return the {@link GVRCursorController} object.
     */
    public GVRCursorController getCursorController() {
        return controller;
    }

    /**
     * Use this method to return a {@link SensorEvent} for use.
     * 
     * @return the {@link SensorEvent} object.
     */
    static SensorEvent obtain() {
        final SensorEvent event;
        synchronized (recyclerLock) {
            event = recyclerTop;
            if (event == null) {
                return new SensorEvent();
            }
            recyclerTop = event.next;
            recyclerUsed -= 1;
        }
        event.next = null;
        return event;
    }

    /**
     * Recycle the {@link SensorEvent} object.
     * 
     * Make sure that the object is not used after this call.
     */
    final void recycle() {
        synchronized (recyclerLock) {
            if (recyclerUsed < MAX_RECYCLED) {
                recyclerUsed++;
                next = recyclerTop;
                recyclerTop = this;
            }
        }
    }
}