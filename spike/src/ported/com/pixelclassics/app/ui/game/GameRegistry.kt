package com.pixelclassics.app.ui.game

import com.pixelclassics.app.BuildConfig
import com.pixelclassics.app.R
import com.pixelclassics.app.engine.Game
import com.pixelclassics.app.games.arkanoid.ArkanoidGame
import com.pixelclassics.app.games.asteroids.AsteroidsGame
import com.pixelclassics.app.games.battlecity.BattleCityGame
import com.pixelclassics.app.games.cave.CaveAdventureGame
import com.pixelclassics.app.games.digger.DiggerGame
import com.pixelclassics.app.games.et.ETGame
import com.pixelclassics.app.games.exodus.ExodusGame
import com.pixelclassics.app.games.frogger.FroggerGame
import com.pixelclassics.app.games.lunar.LunarLanderGame
import com.pixelclassics.app.games.minesweeper.MinesweeperGame
import com.pixelclassics.app.games.missilecommand.MissileCommandGame
import com.pixelclassics.app.games.muncher.MuncherGame
import com.pixelclassics.app.games.navalbattle.NavalBattleGame
import com.pixelclassics.app.games.paratrooper.ParatrooperGame
import com.pixelclassics.app.games.pitfall.PitfallGame
import com.pixelclassics.app.games.pitfallclassic.PitfallClassicGame
import com.pixelclassics.app.games.pong.PongGame
import com.pixelclassics.app.games.pushbox.PushboxGame
import com.pixelclassics.app.games.riverraid.RiverRaidGame
import com.pixelclassics.app.games.rogue.RogueGame
import com.pixelclassics.app.games.snake.SnakeGame
import com.pixelclassics.app.games.starwing.StarWingGame
import com.pixelclassics.app.games.superbarsik.SuperBarsikGame
import com.pixelclassics.app.games.tetris.TetrisGame
import com.pixelclassics.app.games.tictactoe.TicTacToeGame
import com.pixelclassics.app.games.saloon.SaloonGame
import com.pixelclassics.app.games.towerclimb.TowerClimbGame

/**
 * Display metadata + factory for a game shown in the Norton Commander
 * panel. `exeName` etc. are purely cosmetic ("authentic" DOS file row).
 */
data class GameEntry(
    val id: String,
    val titleResId: Int,
    val exeName: String,
    val sizeStr: String,
    val dateStr: String,
    val factory: () -> Game,
)

object GameRegistry {

    private val allGames: List<GameEntry> = listOf(
        GameEntry("snake",          R.string.game_snake,           "SNAKE",    "32,768",  "09-12-98") { SnakeGame() },
        // The remaining 14 + Battle City are added as each game is ported.
        // Stubs return a Coming-Soon placeholder game.
        GameEntry("pong",           R.string.game_pong,           "PONG",     "16,384",  "01-01-72") { PongGame() },
        GameEntry("arkanoid",       R.string.game_arkanoid,       "BRIXOUT", "96,256",  "04-17-86") { ArkanoidGame() },
        GameEntry("tetris",         R.string.game_tetris,         "BRIX",   "49,152",  "06-06-84") { TetrisGame() },
        GameEntry("pitfall",        R.string.game_jungle_run,     "JUNGLE",   "131,072", "07-20-82") { PitfallGame() },
        GameEntry("naval_battle",   R.string.game_naval,          "NAVAL",    "847,296", "03-15-90") { NavalBattleGame() },
        GameEntry("missile",        R.string.game_missile,        "MISLDEF",  "128,512", "11-22-80") { MissileCommandGame() },
        GameEntry("rogue",          R.string.game_rogue,          "DUNGEON",    "65,536",  "06-15-80") { RogueGame() },
        GameEntry("et",             R.string.game_et,             "EXTER",       "8,192",   "12-01-82") { ETGame() },
        GameEntry("exodus",         R.string.game_exodus,         "EXODUS",   "112,640", "03-18-91") { ExodusGame() },
        GameEntry("digger",         R.string.game_digger,         "TUNNELER",   "72,448",  "04-15-83") { DiggerGame() },
        GameEntry("frogger",        R.string.game_frogger,        "HOPPER",  "32,256",  "09-05-81") { FroggerGame() },
        GameEntry("minesweeper",    R.string.game_minesweeper,    "MINES",   "24,576",  "10-08-90") { MinesweeperGame() },
        GameEntry("paratrooper",    R.string.game_paratrooper,    "PARATRO",  "64,000",  "06-08-82") { ParatrooperGame() },
        GameEntry("river_raid",     R.string.game_river_raid,     "RIVERUN", "48,128",  "11-10-82") { RiverRaidGame() },
        GameEntry("battle_city",    R.string.game_battle_city,    "STEELFRT", "384,000", "06-12-26") { BattleCityGame() },
        GameEntry("lunar",          R.string.game_lunar,          "LUNAR",    "48,128",  "07-19-69") { LunarLanderGame() },
        GameEntry("asteroids",      R.string.game_asteroids,      "ROCKS",    "32,768",  "11-13-79") { AsteroidsGame() },
        GameEntry("pushbox",        R.string.game_pushbox,        "PUSHBOX",  "16,384",  "03-12-81") { PushboxGame() },
        GameEntry("muncher",        R.string.game_muncher,        "MUNCHER",  "65,536",  "05-22-80") { MuncherGame() },
        GameEntry("pitfall_classic",R.string.game_pitfall_classic,"PITJUMP",  "131,072", "09-20-82") { PitfallClassicGame() },
        GameEntry("star_wing",      R.string.game_star_wing,      "STARSWRM", "32,768",  "10-15-81") { StarWingGame() },
        GameEntry("cave",           R.string.game_cave,           "CAVEQST",   "8,192",   "03-11-77") { CaveAdventureGame() },
        GameEntry("tic_tac_toe",    R.string.game_tic_tac_toe,    "TICTAC",    "4,096",   "06-11-52") { TicTacToeGame() },
        GameEntry("saloon",         R.string.game_saloon,         "SALOON",    "98,304",  "10-14-83") { SaloonGame() },
        GameEntry("tower_climb",    R.string.game_tower_climb,    "IRONCLMB",  "40,960",  "07-09-81") { TowerClimbGame() },
        GameEntry("super_barsik",   R.string.game_super_barsik,   "CATBROS",   "262,144", "10-18-85") { SuperBarsikGame() },
    )

    private val freeIds = setOf("snake", "lunar", "tetris", "asteroids")

    /**
     * FREE is not a shelf of disabled buttons: it is a clean four-game edition.
     * The full build exposes the entire current registry from the same source.
     */
    val games: List<GameEntry> = if (BuildConfig.FREE_EDITION) {
        allGames.filter { it.id in freeIds }
    } else {
        allGames
    }

    fun byId(id: String): GameEntry? = games.firstOrNull { it.id == id }

    private fun comingSoon(id: String, titleRes: Int, exe: String, size: String, date: String) =
        GameEntry(id, titleRes, exe, size, date) { com.pixelclassics.app.games.ComingSoonGame(titleRes) }
}
