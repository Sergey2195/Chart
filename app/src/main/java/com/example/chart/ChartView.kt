package com.example.chart

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import kotlin.properties.Delegates

class ChartView(
    context: Context,
    attrSet: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : View(context, attrSet, defStyleAttr, defStyleRes) {
    constructor(context: Context, attrSet: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrSet,
        defStyleAttr,
        R.style.DefaultChartStyle
    )

    constructor(context: Context, attrSet: AttributeSet?) : this(
        context,
        attrSet,
        R.attr.chartStyle
    )

    constructor(context: Context) : this(context, null)

    private var data: Array<Int> = arrayOf(0, 0, 0, 0, 0, 0)
    private lateinit var gridPaint: Paint
    private lateinit var chartPaint: Paint
    private lateinit var arrowPain: Paint
    private lateinit var dotsPaint: Paint
    private lateinit var chartValuesPaint: Paint
    private val viewRect = RectF(0f, 0f, 0f, 0f)
    private var numberOfPoints = 0
    private lateinit var bottomArrow: ArrowCoordinates
    private val bottomArrowPath = Path()
        .also { it.fillType = Path.FillType.EVEN_ODD }
    private lateinit var topArrow: ArrowCoordinates
    private val topArrowPath = Path()
        .also { it.fillType = Path.FillType.EVEN_ODD }
    private var cellWidth = 0
    private var cellHeight = 0f
    private var min = 0f
    private var max = 0f
    private val dataDots: ArrayList<Dot> = arrayListOf()


    private var chartColor by Delegates.notNull<Int>()
    private var dotsColor by Delegates.notNull<Int>()
    private var valuesSize by Delegates.notNull<Float>()
    private var chartLineWidth by Delegates.notNull<Float>()

    init {
        if (attrSet != null) {
            initAttributes(context, attrSet, defStyleAttr, defStyleRes)
        }
        initPaint()
        updateViewSize()
    }

    private fun initPaint() {
        gridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        gridPaint.style = Paint.Style.STROKE
        gridPaint.strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            WIDTH_PAINT,
            resources.displayMetrics
        )
        gridPaint.color = Color.BLACK
        arrowPain = Paint(Paint.ANTI_ALIAS_FLAG)
        arrowPain.strokeWidth = 2f
        arrowPain.color = Color.BLACK
        arrowPain.style = Paint.Style.FILL_AND_STROKE
        chartPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        chartPaint.style = Paint.Style.STROKE
        chartPaint.strokeWidth =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                chartLineWidth,
                resources.displayMetrics
            )
        chartPaint.color = chartColor
        dotsPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        dotsPaint.style = Paint.Style.STROKE
        dotsPaint.strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            WIDTH_PAINT,
            resources.displayMetrics
        )
        dotsPaint.color = dotsColor
        chartValuesPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        chartValuesPaint.style = Paint.Style.FILL_AND_STROKE
        chartValuesPaint.strokeWidth =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, resources.displayMetrics)
        chartValuesPaint.textSize = valuesSize
        chartValuesPaint.color = dotsColor
    }

    fun setData(data: Array<Int>) {
        this.data = data
        dataDots.clear()
        updateViewSize()
        updateScore()
        updateDotsData()
        invalidate()
    }

    private fun drawDots(canvas: Canvas) {
        for (dot in dataDots) {
            canvas.drawCircle(dot.x, dot.y, 4f, dotsPaint)
        }
    }

    private fun updateDotsData() {
        numberOfPoints = data.size
        var prevX = viewRect.left
        for (i in 0 until numberOfPoints) {
            prevX += cellWidth
            val y = viewRect.bottom - (data[i] - min) * cellHeight - VERTICAL_DOT_BIAS
            dataDots.add(Dot(prevX, y))
        }
    }

    private fun updateScore() {
        numberOfPoints = data.size
        cellWidth = (viewRect.width() / (numberOfPoints + 1)).toInt()
        min = Int.MAX_VALUE.toFloat()
        max = Int.MIN_VALUE.toFloat()
        for (c in data) {
            min = Math.min(min, c.toFloat())
            max = Math.max(max, c.toFloat())
        }
        var diff = max - min
        if (diff == 0f) {
            diff = 1f
        }
        cellHeight = (viewRect.height() - VERTICAL_DOT_BIAS * 2) / (diff)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawGrid(canvas)
    }

    private fun drawGrid(canvas: Canvas) {
        canvas.drawLine(
            viewRect.left - WIDTH_PAINT,
            viewRect.bottom,
            viewRect.right - ARROW,
            viewRect.bottom,
            gridPaint
        )
        canvas.drawLine(
            viewRect.left,
            viewRect.bottom,
            viewRect.left,
            viewRect.top + ARROW,
            gridPaint
        )
        drawArrow(canvas)
        drawDots(canvas)
        drawLines(canvas)
    }

    private fun drawLines(canvas: Canvas) {
        chartPaint.textSize = valuesSize
        for ((index, dot) in dataDots.withIndex()) {
            canvas.drawText(data[index].toString(), dot.x, dot.y - 50f, chartValuesPaint)
            if (index == 0) {
                continue
            }
            canvas.drawLine(dataDots[index - 1].x, dataDots[index - 1].y, dot.x, dot.y, chartPaint)
        }
    }

    private fun drawArrow(canvas: Canvas) {
        bottomArrowPath.moveTo(bottomArrow.first.x.toFloat(), bottomArrow.first.y.toFloat())
        bottomArrowPath.lineTo(bottomArrow.second.x.toFloat(), bottomArrow.second.y.toFloat())
        bottomArrowPath.lineTo(bottomArrow.third.x.toFloat(), bottomArrow.third.y.toFloat())
        bottomArrowPath.lineTo(bottomArrow.first.x.toFloat(), bottomArrow.first.y.toFloat())
        bottomArrowPath.close()
        canvas.drawPath(bottomArrowPath, arrowPain)

        topArrowPath.moveTo(topArrow.first.x.toFloat(), topArrow.first.y.toFloat())
        topArrowPath.lineTo(topArrow.second.x.toFloat(), topArrow.second.y.toFloat())
        topArrowPath.lineTo(topArrow.third.x.toFloat(), topArrow.third.y.toFloat())
        topArrowPath.lineTo(topArrow.first.x.toFloat(), topArrow.first.y.toFloat())
        topArrowPath.close()
        canvas.drawPath(topArrowPath, arrowPain)
    }

    private fun updateViewSize() {
        viewRect.left = paddingLeft.toFloat() + BIAS_HORIZONTAL
        viewRect.right = (width - paddingLeft).toFloat()
        viewRect.top = paddingTop.toFloat()
        viewRect.bottom = (height - paddingBottom).toFloat() - BIAS_VERTICAL
        bottomArrow = ArrowCoordinates(
            first = Point(viewRect.right.toInt(), viewRect.bottom.toInt()),
            second = Point(
                (viewRect.right - ARROW).toInt(),
                (viewRect.bottom - (ARROW / 2)).toInt()
            ),
            third = Point((viewRect.right - ARROW).toInt(), (viewRect.bottom + (ARROW / 2)).toInt())
        )
        topArrow = ArrowCoordinates(
            first = Point(viewRect.left.toInt(), viewRect.top.toInt()),
            second = Point((viewRect.left - (ARROW / 2)).toInt(), (viewRect.top + ARROW).toInt()),
            third = Point((viewRect.left + ARROW / 2).toInt(), (viewRect.top + ARROW).toInt())
        )
    }

    private fun initAttributes(
        context: Context,
        attrSet: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) {
        val typedArray = context.obtainStyledAttributes(
            attrSet,
            R.styleable.ChartView,
            defStyleAttr,
            defStyleRes
        )
        chartColor = typedArray.getColor(R.styleable.ChartView_chartColor, Color.DKGRAY)
        dotsColor = typedArray.getColor(R.styleable.ChartView_dotsColor, Color.BLACK)
        valuesSize = typedArray.getInt(R.styleable.ChartView_valuesSize, 50).toFloat()
        chartLineWidth = typedArray.getInt(R.styleable.ChartView_charLineWidth, 1).toFloat()
        typedArray.recycle()
    }

    companion object {
        private const val BIAS_VERTICAL = 20
        private const val BIAS_HORIZONTAL = 20
        private const val WIDTH_PAINT = 3f
        private const val ARROW = 30f
        private const val VERTICAL_DOT_BIAS = 100f
    }
}