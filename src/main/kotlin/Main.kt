import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.input.MouseButton
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import tornadofx.*

class Field(var mark: Mark = Mark.Unmarked) : Rectangle() {
    init {
        updateFill()

        setOnMouseClicked { mouseEvent ->
            if (mouseEvent.button == MouseButton.PRIMARY)
                toggleLeftMark()
            else if (mouseEvent.button == MouseButton.SECONDARY)
                toggleRightMark()
        }
    }

    private fun toggleLeftMark() {
        mark = when (mark) {
            Mark.LeftMarked -> Mark.Unmarked
            Mark.RightMarked -> Mark.LeftMarked
            Mark.Unmarked -> Mark.LeftMarked
        }
        updateFill()
    }

    private fun toggleRightMark() {
        mark = when (mark) {
            Mark.LeftMarked -> Mark.RightMarked
            Mark.RightMarked -> Mark.Unmarked
            Mark.Unmarked -> Mark.RightMarked
        }
        updateFill()
    }

    private fun updateFill() {
        fill = mark.defaultColors()
    }
}

enum class Mark {
    LeftMarked, RightMarked, Unmarked;

    fun defaultColors(): Color {
        return when(this) {
            LeftMarked -> Color.RED
            RightMarked -> Color.BLUE
            Unmarked -> Color.GRAY
        }
    }
}

class Check(private val side: Int) : Region() {
    private var fieldSpacing = 20.0

    // This builds the [side]x[side] board
    private val fields = VBox().apply {
        spacing = fieldSpacing
        alignment = Pos.CENTER

        repeat(side) {
            children.add(HBox().apply {
                spacing = 20.0
                alignment = Pos.CENTER

                repeat(side) {
                    children.add(Field().apply {
                        height = 100.0
                        width = 100.0
                    })
                }
            })
        }
    }

    // Checks victory conditions for a specified mark
    fun victoryEligible(mark: Mark): Boolean {
        val horizontalWin: Boolean = fields.children.any { row: Node ->
            (row as HBox).children.all { f: Node ->
                (f as Field).mark == mark
            }
        }

        val verticalWin: Boolean = (0 until side).any { i ->
            fields.children.all { row ->
                ((row as HBox).children[i] as Field).mark == mark
            }
        }

        val diagonalWin: Boolean = (0 until side).all { i ->
            ((fields.children[i] as HBox).children[i] as Field).mark == mark
        }

        val reverseDiagonalWin: Boolean = (0 until side).all { i ->
            ((fields.children[i] as HBox).children[side - i - 1] as Field).mark == mark
        }

        return horizontalWin || verticalWin || diagonalWin || reverseDiagonalWin
    }

    fun victoryResult(): String {
        return if (victoryEligible(Mark.LeftMarked))
            "Red wins!"
        else if (victoryEligible(Mark.RightMarked))
            "Blue wins!"
        else
            "Nobody wins!"
    }

    fun autoResize() {
        val fieldHeight = (height - (side - 1) * fieldSpacing) / side + 1
        val fieldWidth = (width - (side - 1) * fieldSpacing) / side

        fields.children.forEach { row ->
            (row as HBox).children.forEach { field ->
                (field as Field).height = fieldHeight
                field.width = fieldWidth
            }
        }
    }

    override fun layoutChildren() {
        super.layoutChildren()
        autoResize()
    }

    init {
        children.add(fields)
    }
}

class CheckeredApp : App(MainView::class)
class MainView : View() {
    private val check = Check(5)

    override val root = vbox {
        padding = Insets(20.0)
        spacing = 20.0
        add(check)

        button("isVictory?") {
            action {
                println(check.victoryResult())
                check.autoResize()
            }
        }

        alignment = Pos.CENTER
        title = "Red Blue Unmarked"
    }
}


fun main() {
    launch<CheckeredApp>()
}