// Project 1: Game of Life in Kotlin
// CS4411 Programming Languages

package com.example.demo.view
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import tornadofx.*
import kotlinx.coroutines.*

class MyApp: App(MainView::class)

class MainView : View() {
    override val root = vbox{
        label("Game of Life")
        children.style { fontSize = 30.px}
        style {padding = box(10.px) }
    }
    private val grid = GridPane()
    private var theWorld: World
    private var padding = 2.0
    private var size = 10
    private var options = hbox {
        button("Previous generation").setOnAction { prevGeneration() }
        button("Next generation").setOnAction { nextGeneration() }
    }

    init {
        //size of world
        theWorld = World(size)

        //add grid, spacing and buttons
        root.add(grid)
        grid.hgap = padding
        grid.vgap = padding
        root.add(options)
        borderpane{
            center = grid
            bottom = options
        }

        //starting cells
        theWorld.currState[3][1].state = true
        theWorld.currState[4][2].state = true
        theWorld.currState[5][2].state = true
        theWorld.currState[6][2].state = true
        theWorld.currState[6][3].state = true
        theWorld.currState[6][4].state = true
        theWorld.currState[5][4].state = true
        theWorld.currState[5][5].state = true
        theWorld.currState[5][7].state = true

        //add starting cells
        updateWorld()

        //make the prevState the same as the currState
        theWorld.processWorld({ xPos, yPos ->
            theWorld.prevState[xPos][yPos].state = theWorld.currState[xPos][yPos].state
        }, {})
    }

    private fun updateWorld(){
        theWorld.processWorld({ xPos, yPos ->
            val state = theWorld.currState[xPos][yPos].state
            val rectangle = Rectangle(30.0,30.0, if (state) Color.BLACK else Color.LIGHTGRAY)
            grid.add(rectangle, xPos, yPos)
        }, {})
    }

    private fun neighbourCoordinates(xPos:Int, yPos:Int) = arrayOf(
        Pair(xPos - 1,yPos - 1),
        Pair(xPos, yPos - 1),
        Pair(xPos + 1, yPos - 1),
        Pair(xPos - 1, yPos),
        Pair(xPos + 1, yPos),
        Pair(xPos - 1, yPos + 1),
        Pair(xPos, yPos + 1),
        Pair(xPos + 1, yPos + 1))

    private fun Pair<Int,Int>.inBounds() =
            !((first < 0) or (second < 0) or (first > size-1) or (second > size-1))
    private fun Pair<Int,Int>.isAlive() =
            if (theWorld.currState[first][second].state) 1 else 0

    private fun checkNeighbours(){
        theWorld.processWorld({ xPos, yPos ->
            val neighbours = neighbourCoordinates(xPos, yPos).filter{it.inBounds()}
            theWorld.neighboursCount[xPos][yPos].count = neighbours.map{ it.isAlive()}.sum()
        }, {})
    }

    private fun applyNext(){
        theWorld.processWorld({ xPos, yPos ->
            theWorld.prevState[xPos][yPos].state = theWorld.currState[xPos][yPos].state
            if (theWorld.currState[xPos][yPos].state) {
                when (theWorld.neighboursCount[xPos][yPos].count) {
                    0, 1 -> theWorld.currState[xPos][yPos].state =  false
                    2, 3 -> theWorld.currState[xPos][yPos].state = true
                    else -> theWorld.currState[xPos][yPos].state = false
                }
            } else {
                when (theWorld.neighboursCount[xPos][yPos].count) {
                    3 -> theWorld.currState[xPos][yPos].state = true
                    else -> theWorld.currState[xPos][yPos].state = false
                }
            }
        }, {})
        updateWorld()
    }

    private fun applyPrev() {
        theWorld.processWorld({ xPos, yPos ->
            theWorld.currState[xPos][yPos].state = theWorld.prevState[xPos][yPos].state
        }, {})
        updateWorld()
    }
    private fun nextGeneration() = runBlocking {
        launch {checkNeighbours()}
        launch {applyNext()}
    }
    private fun prevGeneration() = runBlocking {
        launch {applyPrev()}
    }

}

class World(private var size: Int) {
    var currState: Array<Array<Cell>> = newWorld()
    var prevState: Array<Array<Cell>> = newWorld()
    var neighboursCount: Array<Array<Count>> = newCount()

    class Count(var count: Int)
    class Cell(var state: Boolean)

    // make 2D array for cells
    private fun newWorld(): Array<Array<Cell>> {
        return Array(size) { Array(size) { Cell(false) } }
    }

    private fun newCount(): Array<Array<Count>> {
        return Array(size) { Array(size) { Count(0) } }
    }

    //iterate through cells
    fun processWorld(X: (Int, Int) -> Unit, Y: () -> Unit) {
        var x = 0
        var y = 0
        currState.forEach {
            it.forEach { _ ->
                X.invoke(x, y)
                x++
            }
            Y.invoke()
            y++
            x = 0
        }
    }
}