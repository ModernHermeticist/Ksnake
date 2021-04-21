import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ktx.app.KtxApplicationAdapter
import ktx.app.clearScreen
import ktx.graphics.use

enum class CellType {
    EMPTY, APPLE, HEAD, BODY, TAIL
}

enum class MoveDirection {
    UP, DOWN, LEFT, RIGHT
}

data class Cell(var x: Int, var y: Int, val size: Int, var type: CellType, var color: Color)

const val SCREEN_WIDTH = 720
const val SCREEN_HEIGHT = 720
const val GRID_SIZE = 16
const val CELL_SIZE = SCREEN_WIDTH / GRID_SIZE

fun main() {
    val config = LwjglApplicationConfiguration().apply {
        width = SCREEN_WIDTH
        height = SCREEN_HEIGHT
    }

    LwjglApplication(MyGame(), config)
}

class MyGame : KtxApplicationAdapter {
    private lateinit var renderer: ShapeRenderer

    var cellList = ArrayList<Cell>()
    var snake = ArrayList<Cell>()
    var apple = Cell(0,0,0, CellType.APPLE, Color.GREEN)

    var delay = 0
    var dead = false

    var lastDirection: MoveDirection = MoveDirection.UP


    override fun create() {
        renderer = ShapeRenderer()
        cellList = generate()
        apple = placeApple(cellList)
        snake = generateSnake();
    }

    override fun render() {
        handleInput()
        logic()
        draw()
    }

    private fun handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.W)) lastDirection = MoveDirection.UP
        else if (Gdx.input.isKeyPressed(Input.Keys.S)) lastDirection = MoveDirection.DOWN
        else if (Gdx.input.isKeyPressed(Input.Keys.A)) lastDirection = MoveDirection.LEFT
        else if (Gdx.input.isKeyPressed(Input.Keys.D)) lastDirection = MoveDirection.RIGHT
    }

    private fun logic() {
        if (dead) return
        delay++
        if (delay < 30) return
        delay = 0
        for (i in snake.size-1 downTo 1) {
            val newSnakePiece = Cell(snake[i-1].x, snake[i-1].y, snake[i-1].size, snake[i-1].type, snake[i-1].color)
            snake[i] = newSnakePiece
        }
        when (lastDirection) {
            MoveDirection.UP -> {
                snake[0].y += CELL_SIZE
            }
            MoveDirection.DOWN -> {
                snake[0].y -= CELL_SIZE
            }
            MoveDirection.LEFT -> {
                snake[0].x -= CELL_SIZE
            }
            MoveDirection.RIGHT -> {
                snake[0].x += CELL_SIZE
            }
        }

        for (i in 1 until snake.size) {
            if (isTouchingSelf(snake[0], snake[i]) || isTouchingWall(snake[0])) {
                dead = true
                snake.forEach { it.color = Color.PURPLE }
                return
            }
        }

        if (snake[0].x == apple.x && snake[0].y == apple.y) {
            snake[snake.lastIndex].type = CellType.BODY
            snake.add(Cell(snake[snake.lastIndex].x - CELL_SIZE, snake[snake.lastIndex].y, CELL_SIZE, CellType.TAIL, Color.BLUE))
            while (snake.any { it.x == apple.x && it.y == apple.y }) apple = placeApple(cellList)
        }
    }

    private fun draw() {
        clearScreen(0f,0f,0f,0f)
        renderer.use(ShapeRenderer.ShapeType.Filled) {
            renderer.color = apple.color
            renderer.rect(apple.x.toFloat(),apple.y.toFloat(),apple.size.toFloat(),apple.size.toFloat())
            renderer.color = snake[0].color
            snake.forEach {
                renderer.rect(it.x.toFloat(), it.y.toFloat(), it.size.toFloat(), it.size.toFloat())
            }
        }
        renderer.use(ShapeRenderer.ShapeType.Line) {
            renderer.color = Color.WHITE
            cellList.forEach {
                renderer.rect(it.x.toFloat(), it.y.toFloat(), it.size.toFloat(), it.size.toFloat())
            }
        }
    }

}

fun isTouchingSelf(head: Cell, other: Cell) = head.x == other.x && head.y == other.y

fun isTouchingWall(head: Cell) = head.x < 0 || head.x == SCREEN_WIDTH || head.y < 0 || head.y > SCREEN_HEIGHT


fun generate(): ArrayList<Cell> {
    val ret = ArrayList<Cell>()
    for (x in 0..GRID_SIZE) {
        for (y in 0..GRID_SIZE) {
            val cell = Cell(CELL_SIZE * x, CELL_SIZE * y, CELL_SIZE, CellType.EMPTY, Color.BLACK)
            ret.add(cell)
        }
    }
    return ret
}

fun generateSnake(): ArrayList<Cell> {
    val snake = ArrayList<Cell>()
    snake.add(Cell(CELL_SIZE * GRID_SIZE / 2, CELL_SIZE * GRID_SIZE / 2, CELL_SIZE, CellType.HEAD, Color.BLUE))
    snake.add(Cell(CELL_SIZE * GRID_SIZE / 2, CELL_SIZE * ((GRID_SIZE / 2) - 1), CELL_SIZE, CellType.BODY, Color.BLUE))
    snake.add(Cell(CELL_SIZE * GRID_SIZE / 2, CELL_SIZE * ((GRID_SIZE / 2) - 2), CELL_SIZE, CellType.TAIL, Color.BLUE))
    return snake
}

fun placeApple(cellList: ArrayList<Cell>): Cell {
    val apple = cellList.random()
    apple.type = CellType.APPLE
    apple.color = Color.GREEN
    return apple
}
