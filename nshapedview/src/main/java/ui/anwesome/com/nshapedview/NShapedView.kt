package ui.anwesome.com.nshapedview

/**
 * Created by anweshmishra on 15/02/18.
 */
import android.app.Activity
import android.view.*
import android.content.*
import android.graphics.*

class NShapedView(ctx:Context):View(ctx) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val renderer = NShapeRenderer(this)
    override fun onDraw(canvas:Canvas) {
        renderer.render(canvas,paint)
    }
    override fun onTouchEvent(event:MotionEvent):Boolean {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> renderer.handleTap()
        }
        return true
    }
    data class Animator(var view:View, var animated:Boolean = false) {
        fun animate(updatecb: () -> Unit) {
            if(animated) {
                updatecb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                }
                catch(ex:Exception) {

                }
            }
        }
        fun stop() {
            if(animated) {
                animated = false
            }
        }
        fun start() {
            if(!animated) {
                animated = true
                view.postInvalidate()
            }
        }
    }
    data class State(var prevScale:Float = 0f,var dir:Float = 0f, var jDir:Int = 1, var j:Int = 0) {
        val scales:Array<Float> = arrayOf(0f, 0f, 0f)
        fun update(stopcb:(Float)->Unit) {
            scales[j] += 0.1f*dir
            if(Math.abs(scales[j] - prevScale) > 1) {
                scales[j] = prevScale + dir
                j += jDir
                if(j == scales.size || j == -1) {
                    jDir *= -1
                    j += jDir
                    prevScale = scales[j]
                    dir = 0f
                    stopcb(prevScale)
                }
            }
        }
        fun startUpdating(startcb:()->Unit) {
            if(dir == 0f) {
                dir = 1f - 2 * prevScale
                startcb()
            }
        }
    }
    data class NShape(var x:Float, var y:Float, var size:Float) {
        val state = State()
        fun draw(canvas:Canvas, paint:Paint) {
            val deg = 30f
            canvas.save()
            canvas.translate(x,y)
            for(i in 0..1) {
                canvas.save()
                canvas.translate(((size / 2) * Math.sin(deg * Math.PI / 180).toFloat()) * state.scales[1] * (i*2-1), 0f)
                val y_gap = (size / 2) * Math.cos(deg * Math.PI / 180).toFloat()*state.scales[0]
                canvas.drawLine(0f,-y_gap,0f,y_gap,paint)
                val updated_y_gap = (size/2) * state.scales[0]
                canvas.save()
                canvas.translate(0f, y_gap*(i*2-1))
                canvas.rotate(deg * -1 * state.scales[2])
                canvas.drawLine(0f, 0f, 0f, updated_y_gap * state.scales[1]*(1-2*i), paint)
                canvas.restore()
                canvas.restore()
            }
            canvas.restore()
        }
        fun update(stopcb: (Float) -> Unit) {
            state.update(stopcb)
        }
        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }
    }
    data class NShapeRenderer(var view:NShapedView, var time:Int = 0) {
        val animator = Animator(view)
        var nShape:NShape ?= null
        fun render(canvas: Canvas, paint: Paint) {
            if(time == 0) {
                val w = canvas.width.toFloat()
                val h = canvas.height.toFloat()
                nShape = NShape(w/2, h/2, 2*Math.min(w,h)/3)
                paint.strokeWidth = Math.min(w,h)/45
                paint.strokeCap = Paint.Cap.ROUND
                paint.color = Color.parseColor("#673AB7")
            }
            canvas.drawColor(Color.parseColor("#212121"))
            nShape?.draw(canvas, paint)
            time++
            animator.animate {
                nShape?.update {
                    animator.stop()
                }
            }
        }
        fun handleTap() {
            nShape?.startUpdating {
                animator.start()
            }
        }
    }
    companion object {
        fun create(activity: Activity):NShapedView {
            val view = NShapedView(activity)
            activity.setContentView(view)
            return view
        }
    }
}