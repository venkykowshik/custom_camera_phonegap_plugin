// "Therefore those skilled at the unorthodox
// are infinite as heaven and earth,
// inexhaustible as the great rivers.
// When they come to an end,
// they begin again,
// like the days and months;
// they die and are reborn,
// like the four seasons."
//
// - Sun Tsu,
// "The Art of War"

package com.performanceactive.plugins.camera;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Handler to update crop window edges by the move type - Horizontal, Vertical, Corner or Center.<br/>
 */
final class CropWindowMoveHandler {

    //region: Fields and Consts

    /**
     * Handler to get/set the crop window edges.
     */
    private final CropWindowHandler mCropWindowHandler;

    /**
     * The type of crop window move that is handled.
     */
    private final Type mType;

    /**
     * Holds the x and y offset between the exact touch location and the exact handle location that is activated.
     * There may be an offset because we allow for some leeway (specified by mHandleRadius) in activating a handle.
     * However, we want to maintain these offset values while the handle is being dragged so that the handle
     * doesn't jump.
     */
    private final PointF mTouchOffset = new PointF();
    //endregion

    /**
     * @param edgeMoveType the type of move this handler is executing
     * @param horizontalEdge the primary edge associated with this handle; may be null
     * @param verticalEdge the secondary edge associated with this handle; may be null
     * @param cropWindowHandler main crop window handle to get and update the crop window edges
     * @param touchX the location of the initial toch possition to measure move distance
     * @param touchY the location of the initial toch possition to measure move distance
     */
    public CropWindowMoveHandler(Type type, CropWindowHandler cropWindowHandler, float touchX, float touchY) {
        mType = type;
        mCropWindowHandler = cropWindowHandler;
        calculateTouchOffset(touchX, touchY);
    }

    /**
     * Updates the crop window by change in the toch location.<br>
     * Move type handled by this instance, as initialized in creation, affects how the change in toch location
     * changes the crop window position and size.<br>
     * After the crop window position/size is changed by toch move it may result in values that vialate contraints:
     * outside the bounds of the shown bitmap, smaller/larger than min/max size or missmatch in aspect ratio.
     * So a series of fixes is executed on "secondary" edges to adjust it by the "primary" edge movement.<br>
     * Primary is the edge directly affected by move type, secondary is the other edge.<br>
     * The crop window is changed by directly setting the Edge coordinates.
     *
     * @param x the new x-coordinate of this handle
     * @param y the new y-coordinate of this handle
     * @param bounds the bounding rectangle of the image
     * @param parentView the parent View containing the image
     * @param snapMargin the maximum distance (in pixels) at which the crop window should snap to the image
     * @param fixedAspectRatio is the aspect ration fixed and 'targetAspectRatio' should be used
     * @param aspectRatio the aspect ratio to maintain
     */
    public void move(float x, float y, Rect bounds, float snapMargin, boolean fixedAspectRatio, float aspectRatio) {

        // Adjust the coordinates for the finger position's offset (i.e. the
        // distance from the initial touch to the precise handle location).
        // We want to maintain the initial touch's distance to the pressed
        // handle so that the crop window size does not "jump".
        x += mTouchOffset.x;
        y += mTouchOffset.y;

        if (mType == Type.CENTER) {
            moveCenter(x, y, bounds, snapMargin);
        } else {
            if (fixedAspectRatio) {
                MoveSizeWithFixedAspectRatio(x, y, bounds, snapMargin, aspectRatio);
            } else {
                MoveSizeWithFreeAspectRatio(x, y, bounds, snapMargin);
            }
        }
    }

    //region: Private methods

    /**
     * Calculates the offset of the touch point from the precise location of the specified handle.<br>
     * Save these values in a member variable since we want to maintain this offset as we drag the handle.
     */
    private void calculateTouchOffset(float touchX, float touchY) {

        float touchOffsetX = 0;
        float touchOffsetY = 0;

        RectF rect = mCropWindowHandler.getRect();

        // Calculate the offset from the appropriate handle.
        switch (mType) {
            case TOP_LEFT:
                touchOffsetX = rect.left - touchX;
                touchOffsetY = rect.top - touchY;
                break;
            case TOP_RIGHT:
                touchOffsetX = rect.right - touchX;
                touchOffsetY = rect.top - touchY;
                break;
            case BOTTOM_LEFT:
                touchOffsetX = rect.left - touchX;
                touchOffsetY = rect.bottom - touchY;
                break;
            case BOTTOM_RIGHT:
                touchOffsetX = rect.right - touchX;
                touchOffsetY = rect.bottom - touchY;
                break;
            case LEFT:
                touchOffsetX = rect.left - touchX;
                touchOffsetY = 0;
                break;
            case TOP:
                touchOffsetX = 0;
                touchOffsetY = rect.top - touchY;
                break;
            case RIGHT:
                touchOffsetX = rect.right - touchX;
                touchOffsetY = 0;
                break;
            case BOTTOM:
                touchOffsetX = 0;
                touchOffsetY = rect.bottom - touchY;
                break;
            case CENTER:
                touchOffsetX = rect.centerX() - touchX;
                touchOffsetY = rect.centerY() - touchY;
                break;
        }

        mTouchOffset.x = touchOffsetX;
        mTouchOffset.y = touchOffsetY;
    }

    /**
     * Center move only changes the position of the crop window without changing the size.
     */
    private void moveCenter(float x, float y, Rect imageRect, float snapRadius) {
        RectF rect = mCropWindowHandler.getRect();
        rect.offset(x - rect.centerX(), y - rect.centerY());
        snapEdgesToBounds(rect, imageRect, snapRadius);
        mCropWindowHandler.setRect(rect);
    }

    /**
     * Change the size of the crop window on the required edge (or edges for corner size move) without
     * affecting "secondary" edges.<br>
     * Only the primary edge(s) are fixed to stay within limits.
     */
    private void MoveSizeWithFreeAspectRatio(float x, float y, Rect bounds, float snapMargin) {
        switch (mType) {
            case TOP_LEFT:
                adjustTop(y, bounds, snapMargin, 0, false, false);
                adjustLeft(x, bounds, snapMargin, 0, false, false);
                break;
            case TOP_RIGHT:
                adjustTop(y, bounds, snapMargin, 0, false, false);
                adjustRight(x, bounds, snapMargin, 0, false, false);
                break;
            case BOTTOM_LEFT:
                adjustBottom(y, bounds, snapMargin, 0, false, false);
                adjustLeft(x, bounds, snapMargin, 0, false, false);
                break;
            case BOTTOM_RIGHT:
                adjustBottom(y, bounds, snapMargin, 0, false, false);
                adjustRight(x, bounds, snapMargin, 0, false, false);
                break;
            case LEFT:
                adjustLeft(x, bounds, snapMargin, 0, false, false);
                break;
            case TOP:
                adjustTop(y, bounds, snapMargin, 0, false, false);
                break;
            case RIGHT:
                adjustRight(x, bounds, snapMargin, 0, false, false);
                break;
            case BOTTOM:
                adjustBottom(y, bounds, snapMargin, 0, false, false);
            default:
                break;
        }
    }

    /**
     * Change the size of the crop window on the required "primary" edge WITH affect to relevant "secondary"
     * edge via aspect ratio.<br>
     * Example: change in the left edge (primary) will affect top and bottom edges (secondary) to preserve the
     * given aspect ratio.
     */
    private void MoveSizeWithFixedAspectRatio(float x, float y, Rect bounds, float snapMargin, float aspectRatio) {
        RectF rect = mCropWindowHandler.getRect();
        switch (mType) {
            case TOP_LEFT:
                if (calculateAspectRatio(x, y, rect.right, rect.bottom) < aspectRatio) {
                    adjustTop(y, bounds, snapMargin, aspectRatio, true, false);
                    adjustLeftByAspectRatio(aspectRatio);
                } else {
                    adjustLeft(x, bounds, snapMargin, aspectRatio, true, false);
                    adjustTopByAspectRatio(aspectRatio);
                }
                break;
            case TOP_RIGHT:
                if (calculateAspectRatio(rect.left, y, x, rect.bottom) < aspectRatio) {
                    adjustTop(y, bounds, snapMargin, aspectRatio, false, true);
                    adjustRightByAspectRatio(aspectRatio);
                } else {
                    adjustRight(x, bounds, snapMargin, aspectRatio, true, false);
                    adjustTopByAspectRatio(aspectRatio);
                }
                break;
            case BOTTOM_LEFT:
                if (calculateAspectRatio(x, rect.top, rect.right, y) < aspectRatio) {
                    adjustBottom(y, bounds, snapMargin, aspectRatio, true, false);
                    adjustLeftByAspectRatio(aspectRatio);
                } else {
                    adjustLeft(x, bounds, snapMargin, aspectRatio, false, true);
                    adjustBottomByAspectRatio(aspectRatio);
                }
                break;
            case BOTTOM_RIGHT:
                if (calculateAspectRatio(rect.left, rect.top, x, y) < aspectRatio) {
                    adjustBottom(y, bounds, snapMargin, aspectRatio, false, true);
                    adjustRightByAspectRatio(aspectRatio);
                } else {
                    adjustRight(x, bounds, snapMargin, aspectRatio, false, true);
                    adjustBottomByAspectRatio(aspectRatio);
                }
                break;
            case LEFT:
                adjustLeft(x, bounds, snapMargin, aspectRatio, true, true);
                adjustTopBottomByAspectRatio(aspectRatio);
                break;
            case TOP:
                adjustTop(y, bounds, snapMargin, aspectRatio, true, true);
                adjustLeftRightByAspectRatio(aspectRatio);
                break;
            case RIGHT:
                adjustRight(x, bounds, snapMargin, aspectRatio, true, true);
                adjustTopBottomByAspectRatio(aspectRatio);
                break;
            case BOTTOM:
                adjustBottom(y, bounds, snapMargin, aspectRatio, true, true);
                adjustLeftRightByAspectRatio(aspectRatio);
                break;
            default:
                break;
        }
    }

    /**
     * Check if edges have gone out of bounds (including snap margin), and fix if needed.
     */
    private void snapEdgesToBounds(RectF edges, Rect bounds, float margin) {
        if (edges.left < bounds.left + margin) {
            edges.offset(bounds.left - edges.left, 0);
        }
        if (edges.top < bounds.top + margin) {
            edges.offset(0, bounds.top - edges.top);
        }
        if (edges.right > bounds.right - margin) {
            edges.offset(bounds.right - edges.right, 0);
        }
        if (edges.bottom > bounds.bottom - margin) {
            edges.offset(0, bounds.bottom - edges.bottom);
        }
    }

    /**
     * Get the resulting x-position of the left edge of the crop window given
     * the handle's position and the image's bounding box and snap radius.
     *
     * @param left the position that the left edge is dragged to
     * @param bounds the bounding box of the image that is being cropped
     * @param snapMargin the snap distance to the image edge (in pixels)
     */
    private void adjustLeft(float left, Rect bounds, float snapMargin, float aspectRatio, boolean topMoves, boolean bottomMoves) {

        RectF rect = mCropWindowHandler.getRect();

        float newLeft = left;

        if (newLeft - bounds.left < snapMargin) {
            newLeft = bounds.left;
        }

        // Checks if the window is too small horizontally
        if (rect.right - newLeft < mCropWindowHandler.getMinCropWidth()) {
            newLeft = rect.right - mCropWindowHandler.getMinCropWidth();
        }

        // Checks if the window is too large horizontally
        if (rect.right - newLeft > mCropWindowHandler.getMaxCropWidth()) {
            newLeft = rect.right - mCropWindowHandler.getMaxCropWidth();
        }

        if (newLeft - bounds.left < snapMargin) {
            newLeft = bounds.left;
        }

        // check vertical bounds if aspect ratio is in play
        if (aspectRatio > 0) {
            float newHeight = (rect.right - newLeft) / aspectRatio;

            // Checks if the window is too small vertically
            if (newHeight < mCropWindowHandler.getMinCropHeight()) {
                newLeft = rect.right - mCropWindowHandler.getMinCropHeight() * aspectRatio;
            }

            // Checks if the window is too large vertically
            if (newHeight > mCropWindowHandler.getMaxCropHeight()) {
                newLeft = rect.right - mCropWindowHandler.getMaxCropHeight() * aspectRatio;
            }

            // if top edge moves by aspect ratio check that it is within bounds
            if (topMoves && rect.bottom - newHeight < bounds.top) {
                newLeft = rect.right - (rect.bottom - bounds.top) * aspectRatio;
            }

            // if bottom edge moves by aspect ratio check that it is within bounds
            if (bottomMoves && rect.top + newHeight > bounds.bottom) {
                newLeft = Math.max(newLeft, rect.right - (bounds.bottom - rect.top) * aspectRatio);
            }
        }

        rect.left = newLeft;
        mCropWindowHandler.setRect(rect);
    }

    /**
     * Get the resulting x-position of the right edge of the crop window given
     * the handle's position and the image's bounding box and snap radius.
     *
     * @param right the position that the right edge is dragged to
     * @param bounds the bounding box of the image that is being cropped
     * @param snapMargin the snap distance to the image edge (in pixels)
     */
    private void adjustRight(float right, Rect bounds, float snapMargin, float aspectRatio, boolean topMoves, boolean bottomMoves) {

        RectF rect = mCropWindowHandler.getRect();

        float newRight = right;

        // If close to the edge
        if (bounds.right - newRight < snapMargin) {
            newRight = bounds.right;
        }

        // Checks if the window is too small horizontally
        if (newRight - rect.left < mCropWindowHandler.getMinCropWidth()) {
            newRight = rect.left + mCropWindowHandler.getMinCropWidth();
        }

        // Checks if the window is too large horizontally
        if (newRight - rect.left > mCropWindowHandler.getMaxCropWidth()) {
            newRight = rect.left + mCropWindowHandler.getMaxCropWidth();
        }

        // If close to the edge
        if (bounds.right - newRight < snapMargin) {
            newRight = bounds.right;
        }

        // check vertical bounds if aspect ratio is in play
        if (aspectRatio > 0) {
            float newHeight = (newRight - rect.left) / aspectRatio;

            // Checks if the window is too small vertically
            if (newHeight < mCropWindowHandler.getMinCropHeight()) {
                newRight = rect.left + mCropWindowHandler.getMinCropHeight() * aspectRatio;
            }

            // Checks if the window is too large vertically
            if (newHeight > mCropWindowHandler.getMaxCropHeight()) {
                newRight = rect.left + mCropWindowHandler.getMaxCropHeight() * aspectRatio;
            }

            // if top edge moves by aspect ratio check that it is within bounds
            if (topMoves && rect.bottom - newHeight < bounds.top) {
                newRight = rect.left + (rect.bottom - bounds.top) * aspectRatio;
            }

            // if bottom edge moves by aspect ratio check that it is within bounds
            if (bottomMoves && rect.top + newHeight > bounds.bottom) {
                newRight = Math.min(newRight, rect.left + (bounds.bottom - rect.top) * aspectRatio);
            }
        }

        rect.right = newRight;
        mCropWindowHandler.setRect(rect);
    }

    /**
     * Get the resulting y-position of the top edge of the crop window given the
     * handle's position and the image's bounding box and snap radius.
     *
     * @param top the x-position that the top edge is dragged to
     * @param bounds the bounding box of the image that is being cropped
     * @param snapMargin the snap distance to the image edge (in pixels)
     */
    private void adjustTop(float top, Rect bounds, float snapMargin, float aspectRatio, boolean leftMoves, boolean rightMoves) {

        RectF rect = mCropWindowHandler.getRect();

        float newTop = top;

        if (newTop - bounds.top < snapMargin) {
            newTop = bounds.top;
        }

        // Checks if the window is too small vertically
        if (rect.bottom - newTop < mCropWindowHandler.getMinCropHeight()) {
            newTop = rect.bottom - mCropWindowHandler.getMinCropHeight();
        }

        // Checks if the window is too large vertically
        if (rect.bottom - newTop > mCropWindowHandler.getMaxCropHeight()) {
            newTop = rect.bottom - mCropWindowHandler.getMaxCropHeight();
        }

        if (newTop - bounds.top < snapMargin) {
            newTop = bounds.top;
        }

        // check horizontal bounds if aspect ratio is in play
        if (aspectRatio > 0) {
            float newWidth = (rect.bottom - newTop) * aspectRatio;

            // Checks if the crop window is too small horizontally due to aspect ratio adjustment
            if (newWidth < mCropWindowHandler.getMinCropWidth()) {
                newTop = rect.bottom - (mCropWindowHandler.getMinCropWidth() / aspectRatio);
            }

            // Checks if the crop window is too large horizontally due to aspect ratio adjustment
            if (newWidth > mCropWindowHandler.getMaxCropWidth()) {
                newTop = rect.bottom - (mCropWindowHandler.getMaxCropWidth() / aspectRatio);
            }

            // if left edge moves by aspect ratio check that it is within bounds
            if (leftMoves && rect.right - newWidth < bounds.left) {
                newTop = rect.bottom - (rect.right - bounds.left) / aspectRatio;
            }

            // if right edge moves by aspect ratio check that it is within bounds
            if (rightMoves && rect.left + newWidth > bounds.right) {
                newTop = Math.max(newTop, rect.bottom - (bounds.right - rect.left) / aspectRatio);
            }
        }

        rect.top = newTop;
        mCropWindowHandler.setRect(rect);
    }

    /**
     * Get the resulting y-position of the bottom edge of the crop window given
     * the handle's position and the image's bounding box and snap radius.
     *
     * @param bottom the position that the bottom edge is dragged to
     * @param bounds the bounding box of the image that is being cropped
     * @param snapMargin the snap distance to the image edge (in pixels)
     */
    private void adjustBottom(float bottom, Rect bounds, float snapMargin, float aspectRatio, boolean leftMoves, boolean rightMoves) {

        RectF rect = mCropWindowHandler.getRect();

        float newBottom = bottom;

        if (bounds.bottom - newBottom < snapMargin) {
            newBottom = bounds.bottom;
        }

        // Checks if the window is too small vertically
        if (newBottom - rect.top < mCropWindowHandler.getMinCropHeight()) {
            newBottom = rect.top + mCropWindowHandler.getMinCropHeight();
        }

        // Checks if the window is too small vertically
        if (newBottom - rect.top > mCropWindowHandler.getMaxCropHeight()) {
            newBottom = rect.top + mCropWindowHandler.getMaxCropHeight();
        }

        if (bounds.bottom - newBottom < snapMargin) {
            newBottom = bounds.bottom;
        }

        // check horizontal bounds if aspect ratio is in play
        if (aspectRatio > 0) {
            float newWidth = (newBottom - rect.top) * aspectRatio;

            // Checks if the window is too small horizontally
            if (newWidth < mCropWindowHandler.getMinCropWidth()) {
                newBottom = rect.top + mCropWindowHandler.getMinCropWidth() / aspectRatio;
            }

            // Checks if the window is too large horizontally
            if (newWidth > mCropWindowHandler.getMaxCropWidth()) {
                newBottom = rect.top + mCropWindowHandler.getMaxCropWidth() / aspectRatio;
            }

            // if left edge moves by aspect ratio check that it is within bounds
            if (leftMoves && rect.right - newWidth < bounds.left) {
                newBottom = rect.top + (rect.right - bounds.left) / aspectRatio;
            }

            // if right edge moves by aspect ratio check that it is within bounds
            if (rightMoves && rect.left + newWidth > bounds.right) {
                newBottom = Math.min(newBottom, rect.top + (bounds.right - rect.left) / aspectRatio);
            }
        }

        rect.bottom = newBottom;
        mCropWindowHandler.setRect(rect);
    }

    /**
     * Adjust left edge by current crop window height and the given aspect ratio,
     * the right edge remains in possition while the left adjusts to keep aspect ratio to the height.
     */
    private void adjustLeftByAspectRatio(float aspectRatio) {
        RectF rect = mCropWindowHandler.getRect();
        rect.left = rect.right - rect.height() * aspectRatio;
        mCropWindowHandler.setRect(rect);
    }

    /**
     * Adjust top edge by current crop window width and the given aspect ratio,
     * the bottom edge remains in possition while the top adjusts to keep aspect ratio to the width.
     */
    private void adjustTopByAspectRatio(float aspectRatio) {
        RectF rect = mCropWindowHandler.getRect();
        rect.top = rect.bottom - rect.width() / aspectRatio;
        mCropWindowHandler.setRect(rect);
    }

    /**
     * Adjust right edge by current crop window height and the given aspect ratio,
     * the left edge remains in possition while the left adjusts to keep aspect ratio to the height.
     */
    private void adjustRightByAspectRatio(float aspectRatio) {
        RectF rect = mCropWindowHandler.getRect();
        rect.right = rect.left + rect.height() * aspectRatio;
        mCropWindowHandler.setRect(rect);
    }

    /**
     * Adjust bottom edge by current crop window width and the given aspect ratio,
     * the top edge remains in possition while the top adjusts to keep aspect ratio to the width.
     */
    private void adjustBottomByAspectRatio(float aspectRatio) {
        RectF rect = mCropWindowHandler.getRect();
        rect.bottom = rect.top + rect.width() / aspectRatio;
        mCropWindowHandler.setRect(rect);
    }

    /**
     * Adjust left and right edges by current crop window height and the given aspect ratio,
     * both right and left edges adjusts equally relative to center to keep aspect ratio to the height.
     */
    private void adjustLeftRightByAspectRatio(float aspectRatio) {
        RectF rect = mCropWindowHandler.getRect();
        rect.inset((rect.width() - rect.height() * aspectRatio) / 2, 0);
        mCropWindowHandler.setRect(rect);
    }

    /**
     * Adjust top and bottom edges by current crop window width and the given aspect ratio,
     * both top and bottom edges adjusts equally relative to center to keep aspect ratio to the width.
     */
    private void adjustTopBottomByAspectRatio(float aspectRatio) {
        RectF rect = mCropWindowHandler.getRect();
        rect.inset(0, (rect.height() - rect.width() / aspectRatio) / 2);
        mCropWindowHandler.setRect(rect);
    }

    /**
     * Calculates the aspect ratio given a rectangle.
     */
    private static float calculateAspectRatio(float left, float top, float right, float bottom) {
        return (right - left) / (bottom - top);
    }
    //endregion

    //region: Inner class: Type

    /**
     * The type of crop window move that is handled.
     */
    public enum Type {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        LEFT,
        TOP,
        RIGHT,
        BOTTOM,
        CENTER
    }
    //endregion
}