package org.hyt.hytport.visual.model

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AbstractComposeView
import kotlin.math.min

class HYTClip public constructor(
    context: Context,
    attributeSet: AttributeSet? = null
): AbstractComposeView(context, attributeSet) {

    private var _body: (@Composable () -> Unit)? = null;

    private val _main: RectF = RectF();

    private val _expanded: RectF = RectF();

    private val _out: RectF = RectF();

    private var _corner: Float = 0.0f;

    private val _round: FloatArray = FloatArray(8);

    private val _path: Path = Path();

    private val _exclude: Path = Path();

    private var _expand: Float = 0.0f;

    private val _location: IntArray = intArrayOf(0, 0);

    init {
        _main.left = 0.0f;
        _main.top = 0.0f;
    }

    fun setBody(body: @Composable () -> Unit) {
        _body = body;
    }

    @Composable
    override fun Content() {
        _body?.invoke();
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

    fun expand(expand: Float): Unit {
        _expand = expand;
        val expandRegion = _main.bottom * (1.0f + expand);
        _expanded.top = -expandRegion;
        _expanded.bottom = expandRegion;
    }

    fun setRound(round: Float): Unit {
        _corner = round;
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        _main.right = w.toFloat();
        _main.bottom = h.toFloat();
        _expanded.right = _main.right;
        val expandRegion: Float = _main.bottom * (1.0f + _expand)
        _expanded.bottom = expandRegion;
        _expanded.top = -expandRegion;
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas);
        _path.rewind();
        _exclude.rewind();
        _exclude.addRoundRect(
            _out,
            _round,
            Path.Direction.CW
        );
        _path.addRect(
            _expanded,
            Path.Direction.CW
        );
        _path.op(_exclude, Path.Op.DIFFERENCE);
        canvas?.clipPath(_path);
    }

}