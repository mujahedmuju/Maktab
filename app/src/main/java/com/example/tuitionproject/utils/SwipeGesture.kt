package com.example.tuitionproject.utils

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


/**
 * Here the animation of the moving RecyclerItem adn the colour behind it is implemented
 * Also the methods of the implemented interfaces will be called.
 *
 *
 * The plan is to make it possible that only one f the two (left and right swipe) interfaces can be passed
 * and then only the one swipe gesture will be possible.
 * That doesn't completely works at the moment
 */
class GestureManager : ItemTouchHelper.SimpleCallback {
    //------------Standard Values------------
    private val STANDARD_MARGIN = 40
    private val STANDARD_COLOUR = Color.BLUE
    private val STANDARD_ICON_SIZE_MULTIPLIER = 1
    private val STANDARD_TEXT_SIZE = 60
    private val STANDARD_TEXT_STROKE = 3
    //------------Instance Variables------------
    /**
     * Colour to be shown on left swipe
     */
    private var leftBackgroundColor: ColorDrawable? = null

    /**
     * Colour to be shown on right swipe
     */
    private var rightBackgroundColor: ColorDrawable? = null

    /**
     * Icon for the left swipe action
     */
    private var drawableLeft: Drawable? = null

    /**
     * Icon for the right swipe action
     */
    private var drawableRight: Drawable? = null

    /**
     * The interface which implements the code which will be executed on the left swipe
     */
    private var swipeCallbackLeft: SwipeCallbackLeft?

    /**
     * The interface which implements the code which will be executed on the right swipe
     */
    private var swipeCallbackRight: SwipeCallbackRight?

    /**
     * Used to Multiply the intrinsic width and height of the
     * drawable icon to change its site
     */
    private var iconSizeMultiplier = 0

    /**
     * Text which will be displayed on right swipe
     */
    private var textRight: String? = null

    /**
     * text which ill be displayed on left swipe
     */
    private var textLeft: String? = null

    /**
     * text paint for the left swipe text
     */
    private var textPaintLeft: Paint? = null

    /**
     * text paint for the right swipe text
     */
    private var textPaintRight: Paint? = null

    /**
     * The margin between canvas-border, icon and text
     */
    private var margin = 40
    //------------Constructors------------
    /**
     * Public Constructor to just implement the LeftSwipe
     *
     * @param onLeftSwipe Implementation of the left swipe action
     */
    constructor(onLeftSwipe: SwipeCallbackLeft?) : super(
        0,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        initWithStandardValues()
        swipeCallbackLeft = onLeftSwipe
        swipeCallbackRight = null
    }

    /**
     * Public Constructor to just implement the RightSwipe
     *
     * @param onRightSwipe Implementation of the right swipe action
     */
    constructor(onRightSwipe: SwipeCallbackRight?) : super(
        0,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        initWithStandardValues()
        swipeCallbackRight = onRightSwipe
        swipeCallbackLeft = null
    }

    /**
     * Public Constructor to implement both swipe directions
     *
     * @param onLeftSwipe  Implementation of the left swipe action
     * @param onRightSwipe Implementation of the right swipe action
     */
    constructor(onRightSwipe: SwipeCallbackRight?, onLeftSwipe: SwipeCallbackLeft?) : super(
        0,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        initWithStandardValues()
        swipeCallbackRight = onRightSwipe
        swipeCallbackLeft = onLeftSwipe
    }

    private fun initWithStandardValues() {
        iconSizeMultiplier = STANDARD_ICON_SIZE_MULTIPLIER
        margin = STANDARD_MARGIN
        leftBackgroundColor = ColorDrawable(STANDARD_COLOUR)
        rightBackgroundColor = ColorDrawable(STANDARD_COLOUR)
        textPaintLeft = Paint()
        textPaintLeft!!.color = Color.BLACK
        textPaintLeft!!.style = Paint.Style.FILL_AND_STROKE
        textPaintLeft!!.strokeWidth = STANDARD_TEXT_STROKE.toFloat()
        textPaintLeft!!.textSize = STANDARD_TEXT_SIZE.toFloat()
        textPaintLeft!!.textAlign = Paint.Align.RIGHT
        textPaintRight = Paint()
        textPaintRight!!.color = Color.BLACK
        textPaintRight!!.style = Paint.Style.FILL_AND_STROKE
        textPaintRight!!.strokeWidth = STANDARD_TEXT_STROKE.toFloat()
        textPaintRight!!.textSize = STANDARD_TEXT_SIZE.toFloat()
        textPaintRight!!.textAlign = Paint.Align.LEFT
    }

    //------------ItemTouchHelper Methods------------
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        viewHolder1: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        if (swipeCallbackLeft != null && direction == ItemTouchHelper.LEFT) {
            swipeCallbackLeft!!.onLeftSwipe(position)
        }
//        if (swipeCallbackRight != null && direction == ItemTouchHelper.RIGHT) {
//            swipeCallbackRight!!.onRightSwipe(position)
//        }
    }

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        // Swiping to the right
        if (dX > 0) {
            // Skip drawing for right swipes
            super.onChildDraw(
                canvas,
                recyclerView,
                viewHolder,
                0f,
                dY,
                actionState,
                isCurrentlyActive
            )
            return
        }

        // Draw for left swipes
        drawLeftLayout(canvas, itemView, dX)
        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }


//        // Swiping to the right
//        if (dX > 0) {
//            drawRightLayout(canvas, itemView, dX)
//        } else if (dX < 0) {
//            drawLeftLayout(canvas, itemView, dX)
//        }
//        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//    }


    //------------Drawing Canvas------------
    private fun drawLeftLayout(canvas: Canvas, itemView: View, dX: Float) {
        val backgroundCornerOffset = 0
        if (swipeCallbackLeft != null) {
            //Setting background colours bounds and draw to the canvas
            leftBackgroundColor!!.setBounds(
                itemView.right + dX.toInt() - backgroundCornerOffset,
                itemView.top,
                itemView.right,
                itemView.bottom
            )
            leftBackgroundColor!!.draw(canvas)

            // Draw Icons and Text
            if (drawableLeft != null) {
                // Calculate values
                val drawableHeight = drawableLeft!!.intrinsicHeight * iconSizeMultiplier
                val drawableWidth = drawableLeft!!.intrinsicWidth * iconSizeMultiplier
                val boundRight = itemView.right - margin
                val boundLeft = boundRight - drawableWidth
                val boundTop = itemView.top + (itemView.height - drawableHeight) / 2
                val boundBottom = itemView.bottom - (itemView.height - drawableHeight) / 2
                drawableLeft!!.setBounds(boundLeft, boundTop, boundRight, boundBottom)
                drawableLeft!!.draw(canvas)
                if (textLeft != null) {
                    // If an icon is present, we need to consider its size for the left margin
                    drawTextLeft(canvas, itemView, (drawableWidth + 2 * margin).toDouble())
                }
            } else if (textLeft != null) {
                drawTextLeft(canvas, itemView, margin.toDouble())
            }
        } else {
            leftBackgroundColor!!.setBounds(0, 0, 0, 0)
            rightBackgroundColor!!.setBounds(0, 0, 0, 0)
        }
    }

    private fun drawTextLeft(canvas: Canvas, itemView: View, marginRight: Double) {
        //Getting the text Bounds
        val textBounds = Rect()
        textPaintLeft!!.getTextBounds(textLeft, 0, textLeft!!.length, textBounds)

        //calculating values
        val textMiddle = (textBounds.top - textBounds.top / 2).toFloat()
        val x = (itemView.right - marginRight).toFloat()
        val y = itemView.bottom - itemView.height / 2 - textMiddle

        //Drawing text at coordinates
        canvas.drawText(textLeft!!, x, y, textPaintLeft!!)
    }

    private fun drawRightLayout(canvas: Canvas, itemView: View, dX: Float) {
        val backgroundCornerOffset = 0
        if (swipeCallbackRight != null) {
            // Setting background colours bounds to the canvas
            rightBackgroundColor!!.setBounds(
                itemView.left,
                itemView.top,
                itemView.left + dX.toInt() + backgroundCornerOffset,
                itemView.bottom
            )
            rightBackgroundColor!!.draw(canvas)
            if (drawableRight != null) {
                // Calculate values
                val drawableHeight = drawableRight!!.intrinsicHeight * iconSizeMultiplier
                val drawableWidth = drawableRight!!.intrinsicWidth * iconSizeMultiplier
                val boundLeft = itemView.left + margin
                val boundRight = boundLeft + drawableWidth
                val boundTop = itemView.top + (itemView.height - drawableHeight) / 2
                val boundBottom = itemView.bottom - (itemView.height - drawableHeight) / 2
                drawableRight!!.setBounds(boundLeft, boundTop, boundRight, boundBottom)
                drawableRight!!.draw(canvas)
                if (textRight != null) {
                    drawTextRight(canvas, itemView, (drawableWidth + 2 * margin).toDouble())
                }
            } else if (textRight != null) {
                drawTextRight(canvas, itemView, margin.toDouble())
            }
        } else {
            leftBackgroundColor!!.setBounds(0, 0, 0, 0)
            rightBackgroundColor!!.setBounds(0, 0, 0, 0)
        }
    }


    private fun drawTextRight(canvas: Canvas, itemView: View, marginLeft: Double) {
        //Getting the text Bounds
        val textBounds = Rect()
        textPaintRight!!.getTextBounds(textRight, 0, textRight!!.length, textBounds)

        //calculating values
        val textMiddle = (textBounds.top - textBounds.top / 2).toFloat()
        val x = (itemView.left + marginLeft).toFloat()
        val y = itemView.bottom - itemView.height / 2 - textMiddle

        //Drawing text at coordinates
        canvas.drawText(textRight!!, x, y, textPaintRight!!)
    }
    //------------Setter------------
    /**
     * Setter fort the  background colour of the
     * left swipe action
     *
     * @param color the colour to be used
     */
    fun setBackgroundColorLeft(color: ColorDrawable?) {
        leftBackgroundColor = color
    }

    /**
     * Setter for the colour of the right swipe action
     *
     * @param color the colour to be used
     */
    fun setBackgroundColorRight(color: ColorDrawable?) {
        rightBackgroundColor = color
    }

    /**
     * Setter for the left swipe action icon
     *
     * @param icon the icon to be used
     */
    fun setIconLeft(icon: Drawable?) {
        drawableLeft = icon
    }

    /**
     * Setter for the right swipe action icon
     *
     * @param icon the icon to be used
     */
    fun setIconRight(icon: Drawable?) {
        drawableRight = icon
    }

    /**
     * Sets the colour for both icons
     *
     * @param color the colour to be applied
     */
    fun setIconColor(color: Int) {
        if (drawableLeft != null) {
            drawableLeft!!.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
        if (drawableRight != null) {
            drawableRight!!.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }

    /**
     * Sets the colours for both icons individually
     *
     * @param colorRight colour for the right swipe icon
     * @param colorLeft  colour for the left swipe icon
     */
    fun setIconColor(colorRight: Int, colorLeft: Int) {
        if (drawableLeft != null) {
            drawableLeft!!.setColorFilter(colorLeft, PorterDuff.Mode.SRC_ATOP)
        }
        if (drawableRight != null) {
            drawableRight!!.setColorFilter(colorRight, PorterDuff.Mode.SRC_ATOP)
        }
    }

    /**
     * The the multiply factor for the icon size
     *
     * @param factor the factor
     */
    fun setIconSizeMultiplier(factor: Int) {
        iconSizeMultiplier = factor
    }

    /**
     * Setter for the right swipe action text
     *
     * @param textRight the text which will appear at the side of the icon
     */
    fun setTextRight(textRight: String?) {
        this.textRight = textRight
    }

    /**
     * Setter for the left swipe action text
     *
     * @param textLeft the text which will appear at the side of the icon
     */
    fun setTextLeft(textLeft: String?) {
        this.textLeft = textLeft
    }

    /**
     * Setter for the text size (both swipe directions)
     *
     * @param size
     */
    fun setTextSize(size: Int) {
        textPaintLeft!!.textSize = size.toFloat()
        textPaintRight!!.textSize = size.toFloat()
    }

    /**
     * Setter for two different sizes for both swipe actions
     *
     * @param leftSize  size for the text at left swipe
     * @param rightSize size for the text on right swipe
     */
    fun setTextSize(leftSize: Int, rightSize: Int) {
        textPaintLeft!!.textSize = leftSize.toFloat()
        textPaintRight!!.textSize = rightSize.toFloat()
    }

    /**
     * Sets one colour for both texts
     *
     * @param color the colour to be applied
     */
    fun setTextColor(color: Int) {
        textPaintRight!!.color = color
        textPaintLeft!!.color = color
    }

    /**
     * Sets two different colours for the texts individually
     *
     * @param colorLeft  the colour for the left swipe text
     * @param colorRight the colour for the right swipe text
     */
    fun setTextColor(colorLeft: Int, colorRight: Int) {
        textPaintRight!!.color = colorRight
        textPaintLeft!!.color = colorLeft
    }

    /**
     * Sets the Margin between border - icon - text
     *
     * @param margin the margin
     */
    fun setMargin(margin: Int) {
        this.margin = margin
    }

    /**
     * Sets the text weight/stroke for both texts
     * @param weight
     * An integer which changes the stroke of the text paint
     */
    fun setTextWeight(weight: Float) {
        textPaintRight!!.strokeWidth = weight
        textPaintLeft!!.strokeWidth = weight
    }

    /**
     * Sets the text weight/stroke for both texts individually
     * @param weightLeft
     * An integer which changes the stroke of the text paint
     * @param weightRight
     * An integer which changes the stroke of the text paint
     */
    fun setTextWeight(weightLeft: Float, weightRight: Float) {
        textPaintRight!!.strokeWidth = weightRight
        textPaintLeft!!.strokeWidth = weightLeft
    }
    //------------OnSwipe------------
    /**
     * Setter for the left right callback
     * to set it after initialization
     *
     * @param swipeCallbackRight
     */
    fun setSwipeCallbackRight(swipeCallbackRight: SwipeCallbackRight?) {
        this.swipeCallbackRight = swipeCallbackRight
    }

    /**
     * Used to implement different  Swipe actions in [GestureManager]
     */
    interface SwipeCallbackLeft {
        fun onLeftSwipe(position: Int)
    }

    /**
     * Used to implement different  Swipe actions in [GestureManager]
     */
    interface SwipeCallbackRight {
        fun onRightSwipe(position: Int)
    }

    /**
     * Setter for the left swipe callback
     * to set it after initialization
     *
     * @param swipeCallbackLeft
     */
    fun setSwipeCallbackLeft(swipeCallbackLeft: SwipeCallbackLeft?) {
        this.swipeCallbackLeft = swipeCallbackLeft
    }
}

