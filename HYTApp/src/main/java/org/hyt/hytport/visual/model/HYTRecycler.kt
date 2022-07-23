package org.hyt.hytport.visual.model

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.min

class HYTRecycler(
    context: Context,
    attributeSet: AttributeSet?
): RecyclerView(context, attributeSet) {

    private val _main: RectF = RectF();

    private val _out: RectF = RectF();

    private var _corner: Float = 0.0f;

    private val _round: FloatArray = FloatArray(8);

    private val _path: Path = Path();

    private val _location: IntArray = intArrayOf(0, 0);

    init {
        _main.left = 0.0f;
        _main.top = 0.0f;
    }

    fun setSize(newWidth: Float, newHeight: Float): Unit {
        _out.right = _out.left + newWidth;
        _out.bottom = _out.top + newHeight;
        val corner: Float = min(_out.width(), _out.height()) * _corner;
        _round.fill(corner);
    }

    fun setOffset(newStart: Float, newTop: Float): Unit {
        getLocationInWindow(_location);
        _out.left = newStart - _location[0];
        _out.top = newTop - _location[1];
    }

    fun setRound(round: Float): Unit {
        _corner = round;
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        _main.right = w.toFloat();
        _main.bottom = h.toFloat();
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas);
        _path.rewind();
        _path.addRoundRect(
            _out,
            _round,
            Path.Direction.CW
        );
        _path.addRect(
            _main,
            Path.Direction.CW
        );
        canvas?.clipPath(_path);
    }

}