package com.example.filleword

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.concurrent.schedule


class GameActivity : AppCompatActivity(), View.OnTouchListener {



    private var xInitial = -1f
    private var yInitial = -1f

    private var xDiff = -1f
    private var yDiff = -1f

    private var prevXDiff = -1f
    private var prevYDiff = -1f

    enum class SwipeState { Undefined, Vertical, Horizontal }
    private var swipeState = SwipeState.Undefined

    private var cellWidth = 0

    override fun onBackPressed() {
        System.exit(0)

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        val congrats_layout:LinearLayout=findViewById(R.id.congrats_layout)

        congrats_layout.visibility = View.GONE
        cellWidth = resources.displayMetrics.widthPixels/10

        for (i in 0 until numWords){
            wordArray[i] = Word(words[i])
        }
        val words_grid:GridLayout=findViewById(R.id.words_grid)
        val childCount = words_grid.childCount
        for (i in 0 until childCount){
            val linearLayout: LinearLayout = words_grid.getChildAt(i) as LinearLayout
            for (t in 0 until linearLayout.childCount){
                linearLayout.getChildAt(t).setOnTouchListener(this)
            }
        }
        val params = words_grid.layoutParams as ConstraintLayout.LayoutParams
        params.height = resources.displayMetrics.widthPixels
        words_grid.layoutParams = params


        generateRandomLetters()
    }


    override fun onTouch( v:View , event: MotionEvent): Boolean{
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.background = ContextCompat.getDrawable(this, R.drawable.selected_cell_background)
                xInitial = event.x
                yInitial = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                if(xInitial != -1f && yInitial != -1f){

                    val tag = v.tag.toString()
                    val tagInt = tag.toInt()

                    xDiff = xInitial - event.x
                    yDiff = yInitial - event.y

                    if(swipeState == SwipeState.Undefined || swipeState == SwipeState.Horizontal){
                        when {
                            xDiff > cellWidth -> {
                                // движение влево
                                if(prevXDiff == -1f || prevXDiff != -1f && prevXDiff < xDiff){
                                    selectSingleCell((tagInt - (xDiff / cellWidth).toInt()).toString())
                                    swipeState = SwipeState.Horizontal
                                } else if ( prevXDiff != -1f && prevXDiff > xDiff){
                                    unselectSingleCell((tagInt - (prevXDiff / cellWidth).toInt()).toString())
                                }
                            }
                            (-1) * xDiff > cellWidth -> {
                                // движение вправо
                                if(prevXDiff == -1f || prevXDiff != -1f && prevXDiff > xDiff){
                                    selectSingleCell((tagInt + -1 * (xDiff / cellWidth).toInt()).toString())
                                    swipeState = SwipeState.Horizontal
                                } else if ( prevXDiff != -1f && prevXDiff < xDiff){
                                    unselectSingleCell((tagInt - (prevXDiff / cellWidth).toInt()).toString())
                                }
                            }
                        }
                    }

                    if(swipeState == SwipeState.Undefined || swipeState == SwipeState.Vertical){
                        when {
                            yDiff > cellWidth -> {
                                // движение вверх
                                if(prevYDiff == -1f || prevYDiff != -1f && prevYDiff < yDiff){
                                    selectSingleCell((tagInt - 10*(yDiff/cellWidth).toInt()).toString())
                                    swipeState = SwipeState.Vertical
                                } else if (prevYDiff != -1f && prevYDiff > yDiff){
                                    unselectSingleCell((tagInt - 10*(yDiff/cellWidth).toInt()).toString())
                                }
                            }
                            (-1)*yDiff > cellWidth -> {
                                // движение вниз
                                if(prevYDiff == -1f || prevYDiff != -1f && prevYDiff > yDiff){
                                    selectSingleCell((tagInt + -10*(yDiff/cellWidth).toInt()).toString())
                                    swipeState = SwipeState.Vertical
                                } else if (prevYDiff != -1f && prevYDiff < yDiff){
                                    unselectSingleCell((tagInt - 10*(yDiff/cellWidth).toInt()).toString())
                                }
                            }
                        }
                    }
                    prevXDiff = xDiff
                    prevYDiff = yDiff
                }
            }

            MotionEvent.ACTION_UP -> {
                val tag = v.tag.toString()
                val tagInt = tag.toInt()
                var finalTag = tag

                if(swipeState == SwipeState.Horizontal){
                    finalTag = when {
                        xDiff > cellWidth -> {
                            (tagInt - (xDiff/cellWidth).toInt()).toString()
                        }
                        -1*xDiff > cellWidth -> {
                            (tagInt + -1*(xDiff/cellWidth).toInt()).toString()
                        }
                        else -> tag
                    }
                } else if(swipeState == SwipeState.Vertical){
                    finalTag = when {
                        yDiff > cellWidth -> {
                            (tagInt - 10*(yDiff/cellWidth).toInt()).toString()
                        }
                        -1*yDiff > cellWidth -> {
                            (tagInt + -10*(yDiff/cellWidth).toInt()).toString()
                        }
                        else -> tag
                    }
                }
                checkIfRangeIsValid(v.tag.toString(), finalTag)
            }
        }
        return true
    }

    private fun checkIfRangeIsValid(initTag: String, endTag: String){
        val greenCheck:ImageView=findViewById(R.id.greenCheck)
        val congrats_layout:LinearLayout=findViewById(R.id.congrats_layout)
        val redX:ImageView=findViewById(R.id.redX)
        var found = false
        for(wordObj in wordArray){
            if(wordObj.checkLoc(initTag.toInt(), endTag.toInt(), swipeState == SwipeState.Horizontal)){
                if(wordObj.found){
                    xInitial = -1f
                    yInitial = -1f
                    xDiff = -1f
                    yDiff = -1f
                    prevXDiff = -1f
                    prevYDiff = -1f
                    swipeState = SwipeState.Undefined
                    return
                }

                markCellsAsFound(initTag.toInt(), endTag.toInt(), swipeState == SwipeState.Horizontal)
                wordObj.found = true
                found = true
                break
            }
        }

        if (found){
            greenCheck.visibility = View.VISIBLE
            Timer("delay", false).schedule(500) {
                runOnUiThread{
                    greenCheck.visibility = View.GONE
                }
            }
            var showCongrats = true
            for(wordObj in wordArray){
                if(!wordObj.found){
                    showCongrats = false
                    break
                }
            }
            if (showCongrats){
                congrats_layout.visibility = View.VISIBLE

                //System.exit(0)
            }

        } else {
            redX.visibility = View.VISIBLE
            Timer("delay", false).schedule(500) {
                runOnUiThread{
                    redX.visibility = View.GONE
                }
            }
            unselectCellRange(initTag.toInt(), endTag.toInt(), swipeState == SwipeState.Horizontal)
        }

        xInitial = -1f
        yInitial = -1f
        xDiff = -1f
        yDiff = -1f
        swipeState = SwipeState.Undefined
    }

    private fun unselectCellRange(initTag: Int, endTag: Int, isHorizontal: Boolean){
        var start = initTag
        var end = endTag
        if (endTag < initTag){
            start = endTag
            end = initTag
        }
        if(isHorizontal){
            for (i in start..end){
                unselectSingleCell(i.toString())
            }
        } else {
            for (i in start..end step 10){
                unselectSingleCell(i.toString())
            }
        }
    }

    private fun selectSingleCell(tag: String){
        val words_grid:GridLayout=findViewById(R.id.words_grid)
        val childCount = words_grid.childCount
        for (i in 0 until childCount){
            val linearLayout: LinearLayout = words_grid.getChildAt(i) as LinearLayout
            for (t in 0 until linearLayout.childCount){
                if(linearLayout.getChildAt(t).tag == tag){
                    linearLayout.getChildAt(t).background = ContextCompat.getDrawable(this, R.drawable.selected_cell_background)
                    //linearLayout.getChildAt(t).setBackgroundColor(Color.parseColor("#FF5722"))
                    return
                }
            }
        }
    }


    private fun unselectSingleCell(tag: String){
        val words_grid:GridLayout=findViewById(R.id.words_grid)
        var tagInt = tag.toInt()
        val childCount = words_grid.childCount
        for (i in 0 until childCount){
            val linearLayout: LinearLayout = words_grid.getChildAt(i) as LinearLayout
            for (t in 0 until linearLayout.childCount){
                if(linearLayout.getChildAt(t).tag == tag){
                    if(!foundWordsFlags[tagInt/10][tagInt%10]){
                        linearLayout.getChildAt(t).background = ContextCompat.getDrawable(this, R.drawable.unselected_cell_background)
                    }
                    return
                }
            }
        }
    }

    private fun markCellsAsFound(initTag: Int, endTag: Int, isHorizontal: Boolean){
        var start = initTag
        var end = endTag
        if (endTag < initTag){
            start = endTag
            end = initTag
        }
        if(isHorizontal){
            for (i in start..end){
                foundWordsFlags[i/10][i%10] = true
            }
        } else {
            for (i in start..end step 10){
                foundWordsFlags[i/10][i%10] = true
            }
        }
    }

    private fun generateRandomLetters(){
        gridFlags = Array(gridSize) { BooleanArray(gridSize) { false } }
        foundWordsFlags = Array(gridSize) { BooleanArray(gridSize) { false } }
        val rnd = Random()
        var toggle: Boolean = rnd.nextInt(2) != 0

        for(r in 0 until gridSize){
            for (c in 0 until gridSize){
                gridLetters[r][c] = vocabulary[rnd.nextInt(vocabulary.length)].toString()
            }
        }

        for (w in 0 until words.size){
            var found = false

            while (!found){
                var r = 0
                if(words[w].length < gridSize){
                    r = rnd.nextInt(gridSize - (words[w].length))
                } else if (words[w].length > gridSize){
                    break
                }

                var start = rnd.nextInt(gridSize - 1)

                for (n in 0 until gridSize){
                    var _n = (n + start) % gridSize
                    for (i in r until r + words[w].length ) {
                        if(toggle){
                            if(gridFlags[_n][i] && gridLetters[_n][i] != words[w][i-r].toString()) {
                                break
                            } else if (i == r + words[w].length - 1) {
                                found = true
                            }
                        } else {
                            if(gridFlags[i][_n]&& gridLetters[i][_n] != words[w][i-r].toString()) {
                                break
                            } else if (i == r + words[w].length - 1) {
                                found = true
                            }
                        }
                    }
                    if(found) {
                        if(toggle){
                            wordArray[w].setLoc(_n*10 + r, toggle)
                        } else {
                            wordArray[w].setLoc(r*10 + _n, toggle)
                        }

                        for (i in r until r + words[w].length ) {
                            if(toggle){
                                gridLetters[_n][i] = words[w][i-r].toString()
                                gridFlags[_n][i] = true
                            } else {
                                gridLetters[i][_n] = words[w][i-r].toString()
                                gridFlags[i][_n] = true
                            }
                        }
                        break
                    }
                }
                toggle = !toggle
            }
        }

        val words_grid:GridLayout=findViewById(R.id.words_grid)
        val childCount = words_grid.childCount
        for (i in 0 until childCount){
            val linearLayout: LinearLayout = words_grid.getChildAt(i) as LinearLayout
            for (t in 0 until linearLayout.childCount){
                (linearLayout.getChildAt(t) as TextView).text = gridLetters[i][t]
            }
        }
    }





    companion object {
        val rnds = (0..20).random()
        val programmer:Array<String> = arrayOf("АККАУНТ", "ОШИБКА", "АДМИН", "КОТЛИН", "ГАДЖЕТ", "ДРАЙВЕР")
        val programmer1:Array<String> = arrayOf("ЗАВИСАНИЕ", "УСТАНОВКА", "ПРОБЕЛ", "СМАЙЛ", "ПРИЛОЖЕНИЕ", "ШРИФТ")
        val programmer2:Array<String> = arrayOf("КУРСОР", "ССЫЛКА", "АДМИН", "ИНТЕРНЕТ", "КОРЗИНА", "ФОРУМ")
        val cars:Array<String> = arrayOf("КОЛЕСО","РУЛЬ","ДВИГАТЕЛЬ","ТУРБИНА","СПОЙЛЕР","БАМПЕР")
        val computer:Array<String> = arrayOf("КЛАВИАТУРА","МОНИТОР","МЫШЬ","КОРПУС","ФЛЕШКА","КАБЕЛЬ")
        val kv:Array<String> = arrayOf("ФОРТОЧКА","ПРИХОЖАЯ","КЛЮЧИ", "КРАН", "ПУФИК", "ВЫТЯЖКА")
        val sport:Array<String> = arrayOf("СЛАЛОМ","ШПАГАТ","САНИ", "СНОУБОРД", "ПАРТЕР", "СПАРРИНГ")
        val sport1:Array<String> = arrayOf("ВРАТАРЬ","АВТОСПОРТ","ТАТАМИ", "САЛЬТО", "НАГРАЖДЕНИЕ", "СПАРРИНГ")
        val teatr:Array<String> = arrayOf("ЗАЛ","БАЛКОН","АКТЕР", "ПУАНТЫ", "ТРАКТОВКА", "АКТ")
        val electr:Array<String> = arrayOf("ВАТТ","КАТОД","ШИНА", "НЕЙТРАЛЬ", "СХЕМА", "АМПЕР")
        val math:Array<String> = arrayOf("ЧИСЛО","ФИГУРА","ЛЕММА", "ХОРДА", "МОДУЛЬ", "МИНУС")
        val math1:Array<String> = arrayOf("ЦИЛИНДР","СИНУС","СКАЛЯР", "СИММЕТРИЯ", "ДИАГОНАЛЬ", "ПЛЮС")
        val eat:Array<String> = arrayOf("АЙРАН","СОК","УХА", "БУБЛИК", "КАНАПЕ", "МОЛОКО")
        val eat1:Array<String> = arrayOf("ЛЕЧО","ФУАГРА","КОФЕ", "СТЕЙК", "АДЖИКА", "ОМЛЕТ")
        val decoration:Array<String> = arrayOf("ОБРУЧ","ЮВЕЛИР","БРОШЬ", "БРЕЛОК", "ЧЕКАНКА", "ТОМПАК")
        val kino:Array<String> = arrayOf("КАДР","ПРИКВЕЛ","СПЕЦЭФФЕКТ", "РЕКЛАМА", "ПОКАЗ", "МОНТАЖ")
        val kino1:Array<String> = arrayOf("КИНОЛЯП","КИНОЗАЛ","ТЕМНОТА", "ПОПКОРН", "УЖАСЫ", "ЗВЕЗДА")
        val circus:Array<String> = arrayOf("ЛОШАДЬ","САЛЬТО","ЖОНГЛЁР", "ТРЮК", "МЕДВЕДЬ", "НОМЕР")
        val circus1:Array<String> = arrayOf("АКРОБАТ","МАСКА","ТЮЛЕНЬ", "АМФИТЕАТР", "КЛОУНАДА", "МАСКА")
        val dance:Array<String> = arrayOf("РИТМ","ЖЮРИ","ХОРЕОГРАФ", "ПОЛОНЕЗ", "СВЯЗКА", "ЛАМБАДА")
        val dance1:Array<String> = arrayOf("ТЕКТОНИК","КАДРИЛЬ","ПОЛОНЕЗ", "ГАЛОП", "РАЗМЕР", "ДУЭТ")


        val arrayOfWord = arrayOf(programmer,programmer1,programmer2,cars,computer,kv,sport, sport1, teatr, electr,math,math1,eat,eat1,decoration,kino,kino1,circus,circus1,dance,dance1)


        const val vocabulary = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"
        const val numWords = 6
        const val gridSize = 10


        var gridLetters = Array(gridSize) { Array<String>(gridSize) { "A" } }
        var gridFlags = Array(gridSize) { BooleanArray(gridSize) { false } }
        var foundWordsFlags = Array(gridSize) { BooleanArray(gridSize) { false } }



        val wordArray = Array<Word>(numWords) { Word("") }
        val words = arrayOfWord[rnds]


    }


    fun help(view: View) {
        val help:TextView = findViewById(R.id.textViewHelp)
        help.visibility = View.VISIBLE
        help.text="${words[0]}, ${words[1]}, ${words[2]}, ${words[3]}, ${words[4]}, ${words[5]}"
        Timer("delay", false).schedule(1200) {
            runOnUiThread {
                help.visibility = View.GONE
            }
        }
    }
}