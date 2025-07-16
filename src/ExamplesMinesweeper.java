import java.util.ArrayList;
import java.util.Random;
import java.awt.Color;
import tester.*;
import javalib.impworld.*;
import javalib.worldimages.*;

// Represents a single cell in the Minesweeper board. Tracks its state and neighbors.
class Cell {
  public ArrayList<Cell> neighbors;
  public boolean mine;
  public boolean revealed;
  public boolean flagged;
  public int mineCount;

  // Constructs a new cell with default values
  public Cell() {
    this.neighbors = new ArrayList<Cell>();
    this.mine = false;
    this.revealed = false;
    this.flagged = false;
    this.mineCount = 0;
  }

  // Adds a neighboring cell to this cell's list of neighbors
  // EFFECT: Modifies the neighbors list by adding the given cell
  public void addNeighbor(Cell c) {
    this.neighbors.add(c);
  }

  // Counts the number of mines in neighboring cells
  // EFFECT: Updates this cell's mineCount field
  public void countNeighborMines() {
    int count = 0;
    for (int i = 0; i < this.neighbors.size(); i = i + 1) {
      if (this.neighbors.get(i).mine) {
        count = count + 1;
      }
    }
    this.mineCount = count;
  }

  // Reveals this cell and recursively reveals neighbors if this cell has no
  // adjacent mines
  // EFFECT: Sets this cell's revealed field to true and may reveal neighboring
  // cells
  public void reveal() {
    if (this.revealed || this.flagged) {
      return;
    }
    this.revealed = true;
    if (this.mineCount == 0 && !this.mine) {
      for (int i = 0; i < this.neighbors.size(); i = i + 1) {
        if (!this.neighbors.get(i).revealed) {
          this.neighbors.get(i).reveal();
        }
      }
    }
  }

  // Toggles the flag status of this cell if it's not revealed
  // EFFECT: Inverts the flagged field if the cell is not revealed
  public void toggleFlag() {
    if (!this.revealed) {
      this.flagged = !this.flagged;
    }
  }

  // Creates a visual representation of this cell based on its current state
  public WorldImage draw(int size) {
    if (this.revealed) {
      if (this.mine) {
        return new OverlayImage(
            new RectangleImage(size / 2, size / 2, OutlineMode.SOLID, Color.BLACK),
            new RectangleImage(size, size, OutlineMode.SOLID, Color.LIGHT_GRAY));
      }
      else if (this.mineCount > 0) {
        return new OverlayImage(
            new TextImage(Integer.toString(this.mineCount), size / 2.0, Color.BLUE),
            new RectangleImage(size, size, OutlineMode.SOLID, Color.LIGHT_GRAY));
      }
      else {
        return new RectangleImage(size, size, OutlineMode.SOLID, Color.LIGHT_GRAY);
      }
    }
    else {
      WorldImage outline = new RectangleImage(size, size, OutlineMode.OUTLINE, Color.BLACK);
      WorldImage fill = new RectangleImage(size - 2, size - 2, OutlineMode.SOLID,
          new Color(180, 220, 255));
      WorldImage cellImg = new OverlayImage(fill, outline);
      if (this.flagged) {
        WorldImage flag = new TriangleImage(new Posn(0, size / 2), new Posn(size / 2, 0),
            new Posn(size, size / 2), OutlineMode.SOLID, Color.ORANGE);
        return new OverlayImage(flag, cellImg);
      }
      else {
        return cellImg;
      }
    }
  }
}

// Represents the Minesweeper game world with board management and game logic
class MinesweeperWorld extends World {
  public ArrayList<ArrayList<Cell>> board;
  public int rows;
  public int cols;
  public int mines;
  public int cellSize;
  public boolean gameOver;
  public boolean won;
  public Random rand;
  public int flagsPlaced;
  public int cellsRevealed;
  public int clickCount;
  public boolean selectingDifficulty;
  public int customRows;
  public int customCols;
  public int customMines;
  public String customInput;
  public String customField;
  public int windowWidth;
  public int windowHeight;

  // Constructs a new game world with specified dimensions and mine count
  public MinesweeperWorld(int rows, int cols, int mines) {
    this(rows, cols, mines, new Random());
  }

  // Constructs a new game world with specified dimensions, mine count, and random
  // seed
  public MinesweeperWorld(int rows, int cols, int mines, Random rand) {
    this.rows = rows;
    this.cols = cols;
    this.mines = mines;
    this.cellSize = 30;
    this.gameOver = false;
    this.won = false;
    this.rand = rand;
    this.flagsPlaced = 0;
    this.cellsRevealed = 0;
    this.clickCount = 0;
    this.board = new ArrayList<ArrayList<Cell>>();
    this.selectingDifficulty = true;
    this.customRows = 10;
    this.customCols = 10;
    this.customMines = 10;
    this.customInput = "";
    this.customField = "rows";
    this.windowWidth = 500;
    this.windowHeight = 500;
    this.initBoard();
  }

  // Initializes the game board with cells, places mines randomly, and sets up
  // neighbor relationships
  // EFFECT: Clears and rebuilds the board, places mines, and counts neighboring
  // mines for each cell
  public void initBoard() {
    this.board.clear();
    for (int r = 0; r < this.rows; r = r + 1) {
      ArrayList<Cell> row = new ArrayList<Cell>();
      for (int c = 0; c < this.cols; c = c + 1) {
        row.add(new Cell());
      }
      this.board.add(row);
    }
    ArrayList<Integer> positions = new ArrayList<Integer>();
    for (int i = 0; i < this.rows * this.cols; i = i + 1) {
      positions.add(i);
    }
    for (int i = 0; i < this.mines; i = i + 1) {
      int idx = this.rand.nextInt(positions.size());
      int pos = positions.remove(idx);
      int r = pos / this.cols;
      int c = pos % this.cols;
      this.board.get(r).get(c).mine = true;
    }
    for (int r = 0; r < this.rows; r = r + 1) {
      for (int c = 0; c < this.cols; c = c + 1) {
        Cell cell = this.board.get(r).get(c);
        for (int dr = -1; dr <= 1; dr = dr + 1) {
          for (int dc = -1; dc <= 1; dc = dc + 1) {
            if (!(dr == 0 && dc == 0)) {
              int nr = r + dr;
              int nc = c + dc;
              if (nr >= 0 && nr < this.rows && nc >= 0 && nc < this.cols) {
                cell.addNeighbor(this.board.get(nr).get(nc));
              }
            }
          }
        }
      }
    }
    for (int r = 0; r < this.rows; r = r + 1) {
      for (int c = 0; c < this.cols; c = c + 1) {
        this.board.get(r).get(c).countNeighborMines();
      }
    }
    this.flagsPlaced = 0;
    this.cellsRevealed = 0;
    this.clickCount = 0;
  }

  // Creates the visual scene for the game, including difficulty selection or game
  // board
  public WorldScene makeScene() {
    int width = Math.max(this.cols * this.cellSize, 400);
    int height = Math.max(this.rows * this.cellSize + 40, 300);
    this.windowWidth = width;
    this.windowHeight = height;
    int xOffset = (width - this.cols * this.cellSize) / 2;
    if (this.selectingDifficulty) {
      WorldScene scene = new WorldScene(this.windowWidth, this.windowHeight);
      scene.placeImageXY(new TextImage("Select Difficulty", 30, Color.BLACK), width / 2, 40);
      scene.placeImageXY(new RectangleImage(120, 40, OutlineMode.SOLID, Color.LIGHT_GRAY),
          width / 2, 90);
      scene.placeImageXY(new TextImage("Easy (9x9, 10)", 20, Color.BLACK), width / 2, 90);
      scene.placeImageXY(new RectangleImage(120, 40, OutlineMode.SOLID, Color.LIGHT_GRAY),
          width / 2, 140);
      scene.placeImageXY(new TextImage("Medium (16x16, 40)", 20, Color.BLACK), width / 2, 140);
      scene.placeImageXY(new RectangleImage(120, 40, OutlineMode.SOLID, Color.LIGHT_GRAY),
          width / 2, 190);
      scene.placeImageXY(new TextImage("Hard (30x16, 99)", 20, Color.BLACK), width / 2, 190);
      scene.placeImageXY(new RectangleImage(120, 40, OutlineMode.SOLID, Color.LIGHT_GRAY),
          width / 2, 240);
      scene.placeImageXY(new TextImage("Custom", 20, Color.BLACK), width / 2, 240);
      if (this.customField.equals("")) {
        scene.placeImageXY(new TextImage("Rows: " + this.customRows + "  Cols: " + this.customCols
            + "  Mines: " + this.customMines, 16, Color.BLUE), width / 2, 270);
        scene.placeImageXY(new TextImage("Type " + this.customField + ": " + this.customInput, 16,
            Color.DARK_GRAY), width / 2, 290);
      }
      return scene;
    }
    WorldScene scene = new WorldScene(width, height);
    for (int r = 0; r < this.rows; r = r + 1) {
      for (int c = 0; c < this.cols; c = c + 1) {
        WorldImage img = this.board.get(r).get(c).draw(this.cellSize);
        scene.placeImageXY(img, xOffset + c * this.cellSize + this.cellSize / 2,
            r * this.cellSize + this.cellSize / 2 + 40);
      }
    }
    String info = "Mines: " + this.mines + "  Flags: " + this.flagsPlaced + "  Revealed: "
        + this.cellsRevealed + "  Clicks: " + this.clickCount;
    scene.placeImageXY(new TextImage(info, 18, Color.BLACK), width / 2, 20);
    if (this.gameOver) {
      scene.placeImageXY(new TextImage("Game Over! Click to play again.", 40, Color.RED), width / 2,
          height / 2);
    }
    if (this.won) {
      scene.placeImageXY(new TextImage("You Win! Click to play again.", 40, Color.GREEN), width / 2,
          height / 2);
    }
    return scene;
  }

  // Handles mouse click events for difficulty selection and gameplay
  // EFFECT: Modifies game state based on click location and button type
  public void onMouseClicked(Posn pos, String button) {
    int width = this.windowWidth;
    int height = this.windowHeight;
    int xOffset = (width - this.cols * this.cellSize) / 2;
    if (this.selectingDifficulty) {
      // Easy
      if (pos.x > width / 2 - 60 && pos.x < width / 2 + 60 && pos.y > 70 && pos.y < 110) {
        this.rows = 9;
        this.cols = 9;
        this.mines = 10;
        this.selectingDifficulty = false;
        this.gameOver = false;
        this.won = false;
        this.initBoard();
        return;
      }
      // Medium
      if (pos.x > width / 2 - 60 && pos.x < width / 2 + 60 && pos.y > 120 && pos.y < 160) {
        this.rows = 16;
        this.cols = 16;
        this.mines = 40;
        this.selectingDifficulty = false;
        this.gameOver = false;
        this.won = false;
        this.initBoard();
        return;
      }
      // Hard
      if (pos.x > width / 2 - 60 && pos.x < width / 2 + 60 && pos.y > 170 && pos.y < 210) {
        this.rows = 16;
        this.cols = 30;
        this.mines = 99;
        this.selectingDifficulty = false;
        this.gameOver = false;
        this.won = false;
        this.initBoard();
        return;
      }
      // Custom
      if (pos.x > width / 2 - 60 && pos.x < width / 2 + 60 && pos.y > 220 && pos.y < 260) {
        this.customField = "rows";
        this.customInput = "";
        return;
      }
      return;
    }
    if (this.gameOver || this.won) {
      this.selectingDifficulty = true;
      this.windowWidth = 500;
      this.windowHeight = 500;
      this.initBoard();
      return;
    }
    int c = (pos.x - xOffset) / this.cellSize;
    int r = (pos.y - 40) / this.cellSize;
    if (r < 0 || r >= this.rows || c < 0 || c >= this.cols) {
      return;
    }
    Cell cell = this.board.get(r).get(c);
    this.clickCount = this.clickCount + 1;
    if (button.equals("LeftButton")) {
      if (!cell.flagged && !cell.revealed) {
        cell.reveal();
        this.updateCellsRevealed();
        if (cell.mine) {
          this.gameOver = true;
          this.revealAllMines();
        }
        else {
          this.checkWin();
        }
      }
    }
    else if (button.equals("RightButton")) {
      if (!cell.revealed) {
        cell.toggleFlag();
        this.updateFlagsPlaced();
      }
    }
  }

  // Validates whether a string contains only numeric digits
  boolean isValidInt(String s) {
    if (s.length() == 0) {
      return false;
    }

    for (int i = 0; i < s.length(); i = i + 1) {
      char c = s.charAt(i);
      if (c < '0' || c > '9') {
        return false;
      }

    }
    return true;
  }

  // Converts a string of digits to its integer representation
  int stringToInt(String s) {
    int result = 0;
    for (int i = 0; i < s.length(); i = i + 1) {
      result = result * 10 + (s.charAt(i) - '0');
    }
    return result;
  }

  // Handles keyboard input for custom game configuration
  // EFFECT: Modifies custom input fields and game state based on key pressed
  public void onKeyEvent(String key) {
    if (!this.selectingDifficulty || this.customField.equals("")) {
      return;
    }
    if (key.equals("backspace")) {
      if (this.customInput.length() > 0) {
        this.customInput = this.customInput.substring(0, this.customInput.length() - 1);
      }
      return;
    }
    if (key.equals("enter")) {
      if (this.isValidInt(this.customInput)) {
        int val = this.stringToInt(this.customInput);
        if (this.customField.equals("rows")) {
          this.customRows = val;
          this.customField = "cols";
          this.customInput = "";
        }
        else if (this.customField.equals("cols")) {
          this.customCols = val;
          this.customField = "mines";
          this.customInput = "";
        }
        else if (this.customField.equals("mines")) {
          this.customMines = val;
          this.rows = this.customRows;
          this.cols = this.customCols;
          this.mines = this.customMines;
          this.selectingDifficulty = false;
          this.gameOver = false;
          this.won = false;
          this.initBoard();
        }
      }
      return;
    }
    if (key.length() == 1 && "0123456789".contains(key)) {
      this.customInput = this.customInput + key;
    }
  }

  // Updates the count of revealed cells on the board
  // EFFECT: Sets cellsRevealed to the current number of revealed cells
  public void updateCellsRevealed() {
    int count = 0;
    for (int r = 0; r < this.rows; r = r + 1) {
      for (int c = 0; c < this.cols; c = c + 1) {
        if (this.board.get(r).get(c).revealed) {
          count = count + 1;
        }
      }
    }
    this.cellsRevealed = count;
  }

  // Updates the count of flagged cells on the board
  // EFFECT: Sets flagsPlaced to the current number of flagged cells
  public void updateFlagsPlaced() {
    int count = 0;
    for (int r = 0; r < this.rows; r = r + 1) {
      for (int c = 0; c < this.cols; c = c + 1) {
        if (this.board.get(r).get(c).flagged) {
          count = count + 1;
        }
      }
    }
    this.flagsPlaced = count;
  }

  // Reveals all mine cells on the board when game is lost
  // EFFECT: Sets revealed to true for all cells containing mines
  public void revealAllMines() {
    for (int r = 0; r < this.rows; r = r + 1) {
      for (int c = 0; c < this.cols; c = c + 1) {
        Cell cell = this.board.get(r).get(c);
        if (cell.mine) {
          cell.revealed = true;
        }
      }
    }
  }

  // Checks if the player has won by revealing all non-mine cells
  // EFFECT: Sets won to true if all non-mine cells are revealed
  public void checkWin() {
    for (int r = 0; r < this.rows; r = r + 1) {
      for (int c = 0; c < this.cols; c = c + 1) {
        Cell cell = this.board.get(r).get(c);
        if (!cell.mine && !cell.revealed) {
          return;
        }
      }
    }
    this.won = true;
  }
}

// Test class for Minesweeper game functionality
class ExamplesMinesweeper {
  MinesweeperWorld w3x3;
  MinesweeperWorld w3x3seed;

  // Initializes test data with 3x3 game worlds
  // EFFECT: Creates two test game worlds
  void initData() {
    this.w3x3 = new MinesweeperWorld(3, 3, 2);
    this.w3x3seed = new MinesweeperWorld(3, 3, 2, new Random(1));
  }

  // Tests that the board is properly set up with correct dimensions and mine
  // count
  void testBoardSetup(Tester t) {
    this.initData();
    t.checkExpect(this.w3x3.board.size(), 3);
    t.checkExpect(this.w3x3.board.get(0).size(), 3);
    int mineCount = 0;
    for (int r = 0; r < 3; r = r + 1) {
      for (int c = 0; c < 3; c = c + 1) {
        if (this.w3x3.board.get(r).get(c).mine) {
          mineCount = mineCount + 1;
        }
      }
    }
    t.checkExpect(mineCount, 2);
  }

  // Tests that cells have the correct number of neighbors based on their position
  void testNeighbors(Tester t) {
    this.initData();
    t.checkExpect(this.w3x3.board.get(0).get(0).neighbors.size(), 3);
    t.checkExpect(this.w3x3.board.get(1).get(1).neighbors.size(), 8);
    t.checkExpect(this.w3x3.board.get(2).get(2).neighbors.size(), 3);
  }

  // Tests that revealing cells works correctly including cascade revealing
  void testReveal(Tester t) {
    this.initData();
    for (int r = 0; r < 3; r = r + 1) {
      for (int c = 0; c < 3; c = c + 1) {
        Cell cell = this.w3x3seed.board.get(r).get(c);
        if (!cell.mine && cell.mineCount == 0) {
          cell.reveal();
          for (int rr = 0; rr < 3; rr = rr + 1) {
            for (int cc = 0; cc < 3; cc = cc + 1) {
              Cell other = this.w3x3seed.board.get(rr).get(cc);
              if (!other.mine && (other.mineCount == 0 || other == cell)) {
                t.checkExpect(other.revealed, true);
              }
            }
          }
          return;
        }
      }
    }
  }

  // Tests that flagging functionality toggles correctly
  void testFlag(Tester t) {
    this.initData();
    Cell cell = this.w3x3.board.get(0).get(0);
    t.checkExpect(cell.flagged, false);
    cell.toggleFlag();
    t.checkExpect(cell.flagged, true);
    cell.toggleFlag();
    t.checkExpect(cell.flagged, false);
  }

  // Tests win and loss conditions are properly detected
  void testWinLoss(Tester t) {
    this.initData();
    MinesweeperWorld w = new MinesweeperWorld(2, 2, 1, new Random(2));
    for (int r = 0; r < 2; r = r + 1) {
      for (int c = 0; c < 2; c = c + 1) {
        Cell cell = w.board.get(r).get(c);
        if (!cell.mine) {
          cell.reveal();
        }
      }
    }
    w.checkWin();
    t.checkExpect(w.won, true);
    w = new MinesweeperWorld(2, 2, 1, new Random(2));
    for (int r = 0; r < 2; r = r + 1) {
      for (int c = 0; c < 2; c = c + 1) {
        Cell cell = w.board.get(r).get(c);
        if (cell.mine) {
          cell.reveal();
        }
      }
    }
    w.gameOver = false;
    w.onMouseClicked(new Posn(0, 0), "LeftButton");
    boolean lost = false;
    for (int r = 0; r < 2; r = r + 1) {
      for (int c = 0; c < 2; c = c + 1) {
        if (w.board.get(r).get(c).mine && w.board.get(r).get(c).revealed) {
          lost = true;
        }
      }
    }
    t.checkExpect(lost, true);
  }

  // Launches the Minesweeper game with a 10x10 board and 10 mines
  // EFFECT: Starts the game loop and displays the game window
  void testPlayGame(Tester t) {
    new MinesweeperWorld(10, 10, 10).bigBang(500, 500, 0.1);
  }
}