package com.challengefriends.app

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.content.Context
import kotlin.random.Random

class MainActivity : Activity() {
    private val black = Color.rgb(9, 9, 9)
    private val panel = Color.rgb(25, 25, 25)
    private val gold = Color.rgb(212, 175, 55)
    private val cream = Color.rgb(245, 239, 220)
    private val handler = Handler(Looper.getMainLooper())
    private val prefs by lazy { getSharedPreferences("challenge_friends", Context.MODE_PRIVATE) }
    private var page: LinearLayout? = null
    private var score = 0
    private var seconds = 30
    private var timer: Runnable? = null
    private var playerName = "اللاعب"
    private var puzzle = mutableListOf(1,2,3,4,5,6,7,8,0)
    private var questionIndex = 0
    private var clueIndex = 0
    private var memoryFirst = -1
    private var memoryLocked = false
    private var memoryMatched = mutableSetOf<Int>()
    private val questions = listOf(
        Triple("ما عاصمة اليمن؟", listOf("صنعاء","عدن","تعز","إب"), 0),
        Triple("كم عدد أيام السنة العادية؟", listOf("360","365","366","364"), 1),
        Triple("ما أكبر كوكب في المجموعة الشمسية؟", listOf("الأرض","المريخ","المشتري","الزهرة"), 2),
        Triple("ما الحيوان المعروف بسفينة الصحراء؟", listOf("الحصان","الجمل","الغزال","الفيل"), 1),
        Triple("كم ضلعًا للمثلث؟", listOf("ثلاثة","أربعة","خمسة","ستة"), 0),
        Triple("ما لون المزج بين الأزرق والأصفر؟", listOf("أحمر","أخضر","بنفسجي","برتقالي"), 1),
        Triple("أي عضو يضخ الدم؟", listOf("الرئة","الكبد","القلب","المعدة"), 2)
    )
    private val identities = listOf(
        Pair("النحلة", listOf("حشرة تطير","تصنع العسل","تعيش في خلية")),
        Pair("القمر", listOf("يظهر غالبًا ليلًا","يدور حول الأرض","نراه مضيئًا بسبب الشمس")),
        Pair("الكتاب", listOf("له صفحات","نقرأ ما بداخله","قد يحتوي على قصص")),
        Pair("الأسد", listOf("حيوان مفترس","يعيش في البرية","يسمى ملك الغابة")),
        Pair("المفتاح", listOf("شيء صغير غالبًا","نستخدمه لفتح الأقفال","قد يكون معدنيًا"))
    )
    private val wordItems = listOf("مدرسة","هاتف","حديقة","سيارة","مكتبة","نافذة")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = black
        window.navigationBarColor = black
        playerName = prefs.getString("player_name", "اللاعب") ?: "اللاعب"
        showHome()
    }

    override fun onDestroy() {
        stopTimer()
        super.onDestroy()
    }

    private fun newPage(title: String): LinearLayout {
        val scroll = ScrollView(this).apply { setBackgroundColor(black); isFillViewport = true }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(dp(20), dp(18), dp(20), dp(28))
        }
        scroll.addView(content)
        setContentView(scroll)
        page = content
        if (title.isNotEmpty()) {
            content.addView(text(title, 26f, gold, true).apply { setPadding(0, dp(18), 0, dp(18)) })
        }
        return content
    }

    private fun showHome() {
        stopTimer()
        val p = newPage("")
        p.addView(text("تحدي الأصدقاء", 32f, gold, true).apply { setPadding(0, dp(38), 0, dp(8)) })
        p.addView(text("اللعب يجمعنا... والتحدي يبدأ هنا", 15f, cream, false))
        p.addView(text("أهلًا، $playerName", 16f, gold, true).apply { setPadding(0, dp(22), 0, dp(10)) })
        addButton(p, "تغيير اسم اللاعب", false) { editPlayerName() }
        p.addView(text("اختر تحديك", 20f, gold, true).apply { setPadding(0, dp(24), 0, dp(14)) })
        addGame(p, "🧩 ترتيب الصور", "رتّب القطع من 1 إلى 8") { startPuzzle() }
        addGame(p, "❓ تحدي الأسئلة", "أسئلة عامة بأربعة خيارات") { startQuiz() }
        addGame(p, "💡 من أنا؟", "اكتشف الكلمة من التلميحات") { startWhoAmI() }
        addGame(p, "🧠 تحدي الذاكرة", "اكشف الأزواج المتشابهة") { startMemory() }
        addGame(p, "✔ صح أم خطأ", "أجب بسرعة عن العبارات") { startTrueFalse() }
        addGame(p, "🔤 ترتيب الكلمات", "رتّب الحروف واكتشف الكلمة") { startWordGame() }
        addGame(p, "⚡ سباق الحساب", "حل العمليات قبل انتهاء الوقت") { startMathGame() }
        addButton(p, "🏆 النتائج المحفوظة", false) { showScores() }
        addButton(p, "⚙ الإعدادات", false) { showSettings() }
        addButton(p, "🌐 اللعب عبر الشبكة المحلية", true) { showNetworkInfo() }
        p.addView(text("اللعب الحالي على هذا الهاتف دون إنترنت. الاتصال الجماعي عبر Wi-Fi سيُضاف في المرحلة التالية.", 12f, Color.LTGRAY, false).apply { setPadding(0, dp(20), 0, 0) })
    }

    private fun addGame(p: LinearLayout, title: String, sub: String, action: () -> Unit) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            background = rounded(panel, gold, 1); setPadding(dp(15), dp(14), dp(15), dp(14))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
        box.addView(text(title, 18f, gold, true))
        box.addView(text(sub, 13f, cream, false).apply { setPadding(0, dp(5), 0, 0) })
        box.setOnClickListener { action() }
        p.addView(box, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) })
    }

    private fun editPlayerName() {
        val p = newPage("اسم اللاعب")
        val field = EditText(this).apply {
            setText(playerName); setTextColor(cream); textSize = 18f; gravity = Gravity.CENTER
            setHintTextColor(Color.GRAY); hint = "اكتب اسمك"; setSingleLine(true)
        }
        p.addView(field, LinearLayout.LayoutParams(-1, dp(55)))
        addButton(p, "حفظ الاسم", true) {
            val name = field.text.toString().trim()
            if (name.isNotEmpty()) { playerName = name; prefs.edit().putString("player_name", name).apply(); showHome() }
        }
        addButton(p, "رجوع", false) { showHome() }
    }

    private fun startPuzzle() {
        stopTimer(); score = 0; seconds = 90
        puzzle = mutableListOf(1,2,3,4,5,6,7,8,0)
        repeat(80) {
            val blank = puzzle.indexOf(0)
            val choices = neighbors(blank)
            val swap = choices.random()
            puzzle[blank] = puzzle[swap]; puzzle[swap] = 0
        }
        showPuzzle()
    }

    private fun neighbors(i: Int): List<Int> {
        val r = i / 3; val c = i % 3
        return listOf(r-1 to c, r+1 to c, r to c-1, r to c+1)
            .filter { it.first in 0..2 && it.second in 0..2 }
            .map { it.first * 3 + it.second }
    }

    private fun showPuzzle() {
        val p = newPage("🧩 ترتيب الصور")
        p.addView(text("حرّك القطع حتى يصبح ترتيبها صحيحًا", 14f, cream, false))
        p.addView(text("الوقت: $seconds ثانية   •   الحركات: $score", 16f, gold, true).apply { tag = "status"; setPadding(0, dp(12), 0, dp(12)) })
        val grid = GridLayout(this).apply { columnCount = 3; rowCount = 3; alignmentMode = GridLayout.ALIGN_BOUNDS }
        puzzle.forEachIndexed { index, value ->
            val b = Button(this).apply {
                text = if (value == 0) "" else value.toString()
                textSize = 25f; isAllCaps = false
                setTextColor(if (value == 0) black else gold)
                background = rounded(if (value == 0) black else panel, gold, 2)
                setOnClickListener {
                    val blank = puzzle.indexOf(0)
                    if (index in neighbors(blank) && seconds > 0) {
                        puzzle[blank] = puzzle[index]; puzzle[index] = 0; score++
                        if (puzzle == mutableListOf(1,2,3,4,5,6,7,8,0)) {
                            stopTimer(); saveScore(score = maxOf(100 - score, 10)); showResult("أحسنت! أكملت الترتيب", maxOf(100-score,10))
                        } else showPuzzle()
                    }
                }
            }
            grid.addView(b, GridLayout.LayoutParams().apply {
                width = dp(88); height = dp(88); setMargins(dp(4), dp(4), dp(4), dp(4))
            })
        }
        p.addView(grid)
        addButton(p, "إعادة خلط القطع", false) { startPuzzle() }
        addButton(p, "إنهاء اللعبة", false) { stopTimer(); showHome() }
        startTimer { seconds--; if (seconds <= 0) { stopTimer(); showResult("انتهى الوقت", 0) } else showPuzzle() }
    }

    private fun startQuiz() {
        stopTimer(); score = 0; questionIndex = 0; seconds = 20; showQuestion()
    }

    private fun showQuestion() {
        if (questionIndex >= questions.size) { stopTimer(); saveScore(score); showResult("انتهى تحدي الأسئلة", score); return }
        val q = questions[questionIndex]
        val p = newPage("❓ تحدي الأسئلة")
        p.addView(text("السؤال ${questionIndex+1} من ${questions.size}   •   النقاط: $score", 15f, gold, true))
        p.addView(text("الوقت: $seconds", 15f, cream, false).apply { tag = "timer"; setPadding(0, dp(12), 0, dp(12)) })
        p.addView(text(q.first, 21f, cream, true).apply { setPadding(0, dp(18), 0, dp(18)) })
        q.second.forEachIndexed { i, answer ->
            addButton(p, answer, false) {
                stopTimer()
                if (i == q.third) score += 10
                questionIndex++; seconds = 20; showQuestion()
            }
        }
        addButton(p, "إنهاء", false) { stopTimer(); saveScore(score); showResult("أنهيت التحدي", score) }
        startTimer { seconds--; if (seconds <= 0) { questionIndex++; seconds=20; showQuestion() } else updateTimerText("الوقت: $seconds") }
    }

    private fun startWhoAmI() { stopTimer(); score=0; questionIndex=0; clueIndex=0; showWho() }
    private fun showWho() {
        if (questionIndex >= identities.size) { saveScore(score); showResult("انتهى من أنا؟", score); return }
        val item = identities[questionIndex]
        val p = newPage("💡 من أنا؟")
        p.addView(text("الجولة ${questionIndex+1} من ${identities.size}  •  النقاط: $score", 15f, gold, true))
        p.addView(text(item.second.take(clueIndex+1).joinToString("\n\n"), 19f, cream, true).apply { setPadding(0, dp(30), 0, dp(25)) })
        val field = EditText(this).apply { hint="اكتب تخمينك"; setTextColor(cream); setHintTextColor(Color.GRAY); gravity=Gravity.CENTER }
        p.addView(field, LinearLayout.LayoutParams(-1, dp(55)))
        addButton(p, "تحقق من الإجابة", true) {
            if (field.text.toString().trim().equals(item.first, true)) {
                score += if (clueIndex == 0) 15 else 10; questionIndex++; clueIndex=0; showWho()
            } else Toast.makeText(this, "ليست الإجابة الصحيحة، جرّب مجددًا", Toast.LENGTH_SHORT).show()
        }
        addButton(p, "تلميح إضافي", false) { if (clueIndex < item.second.size-1) clueIndex++; showWho() }
        addButton(p, "تجاوز الكلمة", false) { questionIndex++; clueIndex=0; showWho() }
    }

    private fun startMemory() {
        stopTimer(); score=0; memoryFirst=-1; memoryLocked=false; memoryMatched.clear()
        val values = (listOf("🍎","🚗","⭐","🐱","⚽","🌙") + listOf("🍎","🚗","⭐","🐱","⚽","🌙")).shuffled().toMutableList()
        showMemory(values)
    }
    private fun showMemory(values: MutableList<String>) {
        val p = newPage("🧠 تحدي الذاكرة")
        p.addView(text("النقاط: $score   •   الأزواج: ${memoryMatched.size/2} من 6", 15f, gold, true))
        val grid = GridLayout(this).apply { columnCount=3 }
        values.forEachIndexed { i, v ->
            val b = Button(this).apply {
                text = if (i in memoryMatched || i == memoryFirst) v else "؟"
                textSize=24f; isAllCaps=false; setTextColor(gold); background=rounded(panel,gold,1)
                isEnabled = i !in memoryMatched && !memoryLocked
                setOnClickListener {
                    if (memoryFirst == -1) { memoryFirst=i; showMemory(values) }
                    else if (memoryFirst != i) {
                        val first=memoryFirst
                        if (values[first] == values[i]) { memoryMatched.add(first); memoryMatched.add(i); score+=10; memoryFirst=-1; if(memoryMatched.size==12){saveScore(score);showResult("رائع! وجدت كل الأزواج",score)}else showMemory(values) }
                        else { memoryLocked=true; showMemory(values); handler.postDelayed({ memoryFirst=-1; memoryLocked=false; showMemory(values) }, 850) }
                    }
                }
            }
            grid.addView(b, GridLayout.LayoutParams().apply { width=dp(92);height=dp(78);setMargins(dp(4),dp(4),dp(4),dp(4)) })
        }
        p.addView(grid)
        addButton(p,"إعادة اللعب",false){ startMemory() }
        addButton(p,"رجوع",false){showHome()}
    }

    private fun startTrueFalse() { stopTimer(); questionIndex=0; score=0; showTrueFalse() }
    private val statements = listOf(
        Triple("الشمس نجم", true, "صحيح"),
        Triple("عدد أيام الأسبوع ثمانية", false, "خطأ"),
        Triple("الحوت من الثدييات", true, "صحيح"),
        Triple("القاهرة عاصمة المغرب", false, "خطأ"),
        Triple("الماء يتجمد عند صفر مئوية تقريبًا", true, "صحيح")
    )
    private fun showTrueFalse() {
        if(questionIndex>=statements.size){saveScore(score);showResult("انتهى صح أم خطأ",score);return}
        val s=statements[questionIndex]; val p=newPage("✔ صح أم خطأ")
        p.addView(text("عبارة ${questionIndex+1} من ${statements.size}  •  النقاط: $score",15f,gold,true))
        p.addView(text(s.first,23f,cream,true).apply{setPadding(0,dp(40),0,dp(40))})
        addButton(p,"صح",true){if(s.second)score+=10;questionIndex++;showTrueFalse()}
        addButton(p,"خطأ",false){if(!s.second)score+=10;questionIndex++;showTrueFalse()}
    }

    private fun startWordGame() { stopTimer(); questionIndex=0; score=0; showWord() }
    private fun showWord() {
        if(questionIndex>=wordItems.size){saveScore(score);showResult("انتهى ترتيب الكلمات",score);return}
        val answer=wordItems[questionIndex]
        val scrambled=answer.toList().shuffled().joinToString("  ")
        val p=newPage("🔤 ترتيب الكلمات")
        p.addView(text("كلمة ${questionIndex+1} من ${wordItems.size}",15f,gold,true))
        p.addView(text(scrambled,27f,cream,true).apply{setPadding(0,dp(40),0,dp(35))})
        val field=EditText(this).apply{hint="اكتب الكلمة الصحيحة";setTextColor(cream);setHintTextColor(Color.GRAY);gravity=Gravity.CENTER}
        p.addView(field,LinearLayout.LayoutParams(-1,dp(55)))
        addButton(p,"تحقق",true){if(field.text.toString().trim()==answer){score+=10;questionIndex++;showWord()}else Toast.makeText(this,"حاول مرة أخرى",Toast.LENGTH_SHORT).show()}
        addButton(p,"تخطي",false){questionIndex++;showWord()}
    }

    private fun startMathGame() { stopTimer(); score=0; questionIndex=0; showMath() }
    private fun showMath() {
        if(questionIndex>=8){saveScore(score);showResult("انتهى سباق الحساب",score);return}
        val a=Random.nextInt(2,20);val b=Random.nextInt(2,15);val op=if(Random.nextBoolean())"+" else "−";val answer=if(op=="+")a+b else a-b
        val p=newPage("⚡ سباق الحساب")
        p.addView(text("المسألة ${questionIndex+1} من 8  •  النقاط: $score",15f,gold,true))
        p.addView(text("$a $op $b = ؟",30f,cream,true).apply{setPadding(0,dp(35),0,dp(25))})
        val field=EditText(this).apply{hint="الإجابة";inputType=2;setTextColor(cream);setHintTextColor(Color.GRAY);gravity=Gravity.CENTER}
        p.addView(field,LinearLayout.LayoutParams(-1,dp(55)))
        addButton(p,"إرسال",true){if(field.text.toString().toIntOrNull()==answer)score+=10;questionIndex++;showMath()}
        addButton(p,"تخطي",false){questionIndex++;showMath()}
    }

    private fun showResult(message: String, points: Int) {
        stopTimer()
        val p=newPage("🏆 النتيجة")
        p.addView(text(message,22f,cream,true).apply{setPadding(0,dp(24),0,dp(15))})
        p.addView(text("$points نقطة",34f,gold,true).apply{setPadding(0,dp(10),0,dp(25))})
        addButton(p,"العودة للرئيسية",true){showHome()}
    }

    private fun saveScore(score: Int) {
        val old=prefs.getInt("score_$playerName",0)
        prefs.edit().putInt("score_$playerName",old+score).apply()
    }
    private fun showScores() {
        val p=newPage("🏆 النتائج المحفوظة")
        p.addView(text("اللاعب: $playerName",17f,cream,true))
        p.addView(text("مجموع النقاط: ${prefs.getInt("score_$playerName",0)}",22f,gold,true).apply{setPadding(0,dp(20),0,dp(20))})
        addButton(p,"مسح نقاطي",false){prefs.edit().putInt("score_$playerName",0).apply();showScores()}
        addButton(p,"رجوع",true){showHome()}
    }
    private fun showSettings() {
        val p=newPage("⚙ الإعدادات")
        p.addView(text("يتم حفظ اسم اللاعب والنتائج على هذا الهاتف فقط.",15f,cream,false))
        addButton(p,"تغيير اسم اللاعب",false){editPlayerName()}
        addButton(p,"رجوع",true){showHome()}
    }
    private fun showNetworkInfo() {
        val p=newPage("🌐 اللعب الجماعي")
        p.addView(text("الاتصال الجماعي عبر نقطة اتصال Wi-Fi يحتاج إعداد وضع المستضيف واللاعبين ومزامنة اللعب. هذه الوظيفة لم تُفعّل في هذه النسخة بعد.",16f,cream,false))
        addButton(p,"رجوع",true){showHome()}
    }

    private fun startTimer(tick: () -> Unit) {
        stopTimer()
        timer=object:Runnable{override fun run(){tick();if(seconds>0){handler.postDelayed(this,1000)}}}
        handler.postDelayed(timer!!,1000)
    }
    private fun stopTimer(){timer?.let{handler.removeCallbacks(it)};timer=null}
    private fun updateTimerText(value:String){ page?.findViewWithTag<TextView>("timer")?.text=value }
    private fun addButton(p:LinearLayout,label:String,filled:Boolean,action:()->Unit){
        val b=Button(this).apply{
            text=label;textSize=15f;isAllCaps=false
            setTextColor(if(filled)black else gold)
            background=rounded(if(filled)gold else panel,gold,1)
            layoutDirection=View.LAYOUT_DIRECTION_RTL
            setOnClickListener{action()}
        }
        p.addView(b,LinearLayout.LayoutParams(-1,dp(52)).apply{topMargin=dp(8);bottomMargin=dp(4)})
    }
    private fun text(value:String,size:Float,color:Int,bold:Boolean)=TextView(this).apply{
        text=value;textSize=size;setTextColor(color);gravity=Gravity.CENTER
        layoutDirection=View.LAYOUT_DIRECTION_RTL;textAlignment=View.TEXT_ALIGNMENT_CENTER
        if(bold)typeface=Typeface.DEFAULT_BOLD
    }
    private fun rounded(color:Int,stroke:Int,width:Int)=GradientDrawable().apply{
        setColor(color);cornerRadius=dp(15).toFloat();setStroke(dp(width),stroke)
    }
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
}
