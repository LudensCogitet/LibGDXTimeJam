package com.timejam.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class TimeJam extends ApplicationAdapter {
	private SpriteBatch batch;
	private Texture texture;
	private Texture grid;
	private BitmapFont largeFont;
	private Stage ui;

	private Array<Array<TextureRegion>> graphics;
	public Array<TextureRegion> getGraphics(SPRITE_INFO sprite) {
		return this.graphics.get(sprite.INDEX);
	}

	public static final int boardWidth = 16;
	public static final int boardHeight = 16;
	public static final int logicalScreenWidth = 256;
	public static final int logicalScreenHeight = 256;

	private StretchViewport viewport = new StretchViewport(logicalScreenWidth, logicalScreenHeight);

	private Array<Array<Entity>> level;
	private Array<Array<Entity>> actors;
	private Array<Player> players;

	private Wall wall;
	private LevelData levelData;

	private int currentLevel = 0;
	private int playerStartX;
	private int playerStartY;

	private float flashTime = 0f;
	private boolean showGrid = false;

	private boolean halt = false;

	private int moves = 0;
	private Label movesDisplay;

	private int loops = 0;
	private Label loopsDisplay;

	private Label winLabel;
	private Label loseLabel;

	private static final int SPRITE_COUNT = 8;
	public enum SPRITE_INFO {
		PLAYER_ACTIVE(0, 3),
		PLAYER_WALKING(1, 3),
		WALL_STATIC(2, 2),
		JUNK_IDLE(3, 2),
		ROBOT_IDLE(4, 3),
		HATCH(5, 2),
		EXIT(6, 2),
		BUTTON(7, 2);

		SPRITE_INFO(int index, int frameCount) {
			this.INDEX 		 = index;
			this.FRAME_COUNT = frameCount;
		}

		public final int INDEX;
		public final int FRAME_COUNT;
	}

	enum GAME_STATE {
		PLAYING,
		WON,
		LOST
	}
	private GAME_STATE gameState = GAME_STATE.PLAYING;

	@Override
	public void create () {
		this.batch = new SpriteBatch();
		this.texture = new Texture("spriteSheet.png");
		this.grid = new Texture("grid.png");
		this.generateGraphics();

		this.ui = new Stage(this.viewport);

		FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("PressStart2P.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();

		param.size = 16;
		this.largeFont = gen.generateFont(param);

		Label.LabelStyle labelStyle = new Label.LabelStyle();

		labelStyle.font = this.largeFont;

		this.movesDisplay = new Label("0", labelStyle);
		this.movesDisplay.setColor(Color.CYAN);
		this.movesDisplay.setPosition(TimeJam.logicalScreenWidth - 32, 0);

		this.loopsDisplay = new Label("0", labelStyle);
		this.loopsDisplay.setColor(Color.MAGENTA);
		this.loopsDisplay.setPosition(0, 0);

		this.winLabel = new Label("Level Complete!", labelStyle);
		this.winLabel.setColor(Color.MAGENTA);
		this.winLabel.setVisible(false);

		this.loseLabel = new Label("You Died!", labelStyle);
		this.loseLabel.setColor(Color.MAGENTA);
		this.loseLabel.setVisible(false);

		Table table = new Table();
		table.setFillParent(true);

		VerticalGroup group = new VerticalGroup();

		group.addActor(this.winLabel);
		group.addActor(this.loseLabel);
		table.add(group);

		this.ui.addActor(table);
		this.ui.addActor(this.movesDisplay);
		this.ui.addActor(this.loopsDisplay);

		this.level = new Array<Array<Entity>>(TimeJam.boardHeight);
		this.actors = new Array<Array<Entity>>(TimeJam.boardHeight);
		this.players = new Array<Player>();

		for(int y = 0; y < TimeJam.boardHeight; y++) {
			this.level.add(new Array<Entity>(TimeJam.boardWidth));
			this.actors.add(new Array<Entity>(TimeJam.boardWidth));

			for(int x = 0; x < TimeJam.boardWidth; x++) {
				this.level.get(y).add(null);
				this.actors.get(y).add(null);
			}
		}

		this.wall = new Wall(this, -1, -1);

		this.loadLevel(this.currentLevel);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		float delta = Gdx.graphics.getDeltaTime();

		if(this.gameState == GAME_STATE.PLAYING) {
			this.batch.begin();
			this.batch.setProjectionMatrix(this.viewport.getCamera().combined);

			if(this.showGrid) {
				this.batch.draw(this.grid, 0, 0);
			}
			
			for (int y = this.level.size - 1, arrayY = 0; y >= 0; y--, arrayY++) {
				Array<Entity> row = this.level.get(y);
				for (int x = 0; x < row.size; x++) {
					Entity e = row.get(x);
					if (e != null) {
						e.update(delta);
						this.batch.draw(e.getSprite(), x * TimeJam.boardWidth, y * TimeJam.boardHeight);
					}
				}
			}

			for (int d = 0; d < this.actors.size; d++) {
				Array<Entity> row = this.actors.get(d);
				for (int i = 0; i < row.size; i++) {
					Entity e = row.get(i);
					if (e != null) {
						e.update(delta);
						this.batch.draw(e.getSprite(), e.getX() * TimeJam.boardWidth, e.getY() * TimeJam.boardHeight);
					}
				}
			}

			for (int i = 0; i < this.players.size; i++) {
				Player p = this.players.get(i);

				if (p.isGone()) {
					continue;
				}

				p.update(delta);
				this.batch.draw(p.getSprite(), p.getX() * TimeJam.boardWidth, p.getY() * TimeJam.boardHeight);
			}
			this.batch.end();

			if (this.flashTime > 0) {
				Gdx.gl.glClearColor(1, 1, 1, 1);
				Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
				this.flashTime -= delta;
			}
		} else {
			if(Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
				if(this.gameState == GAME_STATE.WON) {
					this.win();
				} else {
					this.lose();
				}
			}
		}
		this.ui.draw();

		if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
				this.currentLevel = 0;
			}
			this.loadLevel(this.currentLevel);
		} else if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
			Gdx.graphics.setWindowedMode(768, 768);
		} else if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
			Gdx.graphics.setWindowedMode(512, 512);
		} else if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
			Gdx.graphics.setWindowedMode(256, 256);
		} else if(Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
			this.showGrid = !this.showGrid;
		}
	}
	
	@Override
	public void dispose () {
		this.batch.dispose();
		this.texture.dispose();
		this.grid.dispose();
		this.largeFont.dispose();
	}

	@Override
	public void resize(int width, int height) {
		this.viewport.update(width, height, true);
	}

	public void loadLevel(int levelNumber) {
		this.levelData = new LevelData(this);
		int[][] level = this.levelData.levels[levelNumber];

		this.players.clear();

		this.clearActors();

		this.setMoves(0);
		this.setLoops(0);
		this.loseLabel.setVisible(false);
		this.winLabel.setVisible(false);

		for(int y = level.length - 1, arrayY = 0; y >= 0; y--, arrayY++) {
			for(int x = 0; x < level[0].length; x++) {
				this.level.get(y).set(x, null);
				if(level[arrayY][x] == Entity.TYPE.WALL.INDEX) {
					this.wall.setPosition(x, y);
					this.addLevelEntity(this.wall);
				} else if(level[arrayY][x] == Entity.TYPE.EXIT.INDEX){
					this.addLevelEntity(new Exit(this, x, y));
				} else if(level[arrayY][x] == Entity.TYPE.PLAYER.INDEX) {
					this.playerStartX = x;
					this.playerStartY = y;
					this.players.add(new Player(this, x, y, true));
				} else if(level[arrayY][x] == Entity.TYPE.ROBOT.INDEX) {
					this.addActorEntity(new Robot(this, x, y));
				}
			}
		}

		this.levelData.generateLevelEntities(this.currentLevel);
		this.gameState = GAME_STATE.PLAYING;
	}

	public void clearActors() {
		for(int y = 0; y < this.actors.size; y++) {
			for(int x = 0; x < this.actors.get(0).size; x++) {
				this.actors.get(y).set(x, null);
			}
		}
	}

	public boolean canMove(int x, int y) {
		if(x < 0 || x >= TimeJam.boardWidth || y < 0 || y >= TimeJam.boardHeight) {
			return false;
		}

		Entity levelEntity = this.level.get(y).get(x);
		Entity actorEntity = this.actors.get(y).get(x);

		boolean canMove = true;

		if(levelEntity != null && levelEntity.isSolid()) {
			canMove = false;
		}

		if(actorEntity != null && actorEntity.isSolid()) {
			canMove = false;
		}

		return canMove;
	}

	public void movePlayer() {
		for(int i = 0; i < this.players.size; i++) {
			Player player = this.players.get(i);
			player.act();
		}

		this.step();
	}

	private void step() {
		this.setMoves(this.moves + 1);

		for(Array<Entity> row: this.level) {
			for(int i = 0; i < row.size; i++) {
				Entity e = row.get(i);
				if(e != null) {
					e.act();
				}
			}
		}

		for(Array<Entity> row: this.actors) {
			for(int i = 0; i < row.size; i++) {
				Entity e = row.get(i);
				if(e != null) {
					e.think();
				}
			}
		}

		for(Array<Entity> row: this.actors) {
			for(int i = 0; i < row.size; i++) {
				Entity e = row.get(i);
				if(e != null) {
					e.act();
				}
			}
		}
	}

	public void addLevelEntity(Entity entity) {
		this.level.get(entity.getY()).set(entity.getX(), entity);
	}

	public Entity getLevelEntityAt(int x, int y) {
		return this.level.get(y).get(x);
	}

	public void addActorEntity(Entity entity) {
		this.actors.get(entity.getY()).set(entity.getX(), entity);
	}

	public Entity getActorEntityAt(int x, int y) {
		return this.actors.get(y).get(x);
	}

	public void removeActorEntity(Entity actor) {
		this.actors.get(actor.getY()).set(actor.getX(), null);
	}

	public void swapActorEntity(int oldX, int oldY, int newX, int newY) {
		Entity actor = this.actors.get(oldY).get(oldX);
		this.actors.get(newY).set(newX, actor);
		this.actors.get(oldY).set(oldX, null);
	}

	public void timeWarp() {
		for(int i = 0; i < this.players.size; i++) {
			this.players.get(i).timeWarp();
		}

		this.levelData.generateLevelEntities(this.currentLevel);

		// ADD LOGIC FOR RELOADING LEVEL
		this.setMoves(0);
		this.players.add(new Player(this, this.playerStartX, this.playerStartY, true));
		this.halt = true;
		this.flashTime = 0.1f;
		this.setLoops(this.loops + 1);
	}

	public void win() {
		if(this.gameState == GAME_STATE.PLAYING) {
			this.halt = true;
			this.gameState = GAME_STATE.WON;
			this.winLabel.setVisible(true);
		} else if(this.gameState == GAME_STATE.WON) {
			if(this.currentLevel == this.levelData.levels.length -1) {
				this.currentLevel = 0;
			} else {
				this.currentLevel++;
			}

			this.loadLevel(this.currentLevel);
		}
	}

	public void lose() {
		if(this.gameState == GAME_STATE.PLAYING) {
			this.halt = true;
			this.gameState = GAME_STATE.LOST;
			this.loseLabel.setVisible(true);
		} else if(this.gameState == GAME_STATE.LOST) {
			this.loadLevel(this.currentLevel);
		}
	}

	public Player findPlayerAt(int x, int y) {
		for(int i = 0; i < this.players.size; i++) {
			Player player = this.players.get(i);
			if(player.getX() == x && player.getY() == y) {
				return player;
			}
		}

		return null;
	}

	public Player findNearestPlayer(int x, int y) {
		Vector2 origin = new Vector2(x, y);
		Player chosenPlayer = null;
		Player currentPlayer = null;

		for(int i = 0; i < this.players.size; i++) {
			currentPlayer = this.players.get(i);
			if (!currentPlayer.isGone()) {
				if (chosenPlayer == null) {
					chosenPlayer = currentPlayer;
				} else if (currentPlayer.getPosition().dst(origin) < chosenPlayer.getPosition().dst(origin)) {
					chosenPlayer = currentPlayer;
				}
			}
		}

		return chosenPlayer;
	}

	public void setMoves(int moves) {
		this.moves = moves;
		this.movesDisplay.setText(Integer.toString(this.moves));
		this.movesDisplay.pack();
	}

	public void setLoops(int loops) {
		this.loops = loops;
		this.loopsDisplay.setText(Integer.toString(this.loops));
		this.loopsDisplay.pack();
	}

	private void generateGraphics() {
		TextureRegion[][] tiles = this.fixBleeding(TextureRegion.split(this.texture, 16, 16));
		if(TimeJam.SPRITE_COUNT != tiles.length) {
			throw new java.lang.Error("Sprite sheet contains more rows than number of sprites");
		}

		this.graphics = new Array<Array<TextureRegion>>(tiles.length);

		Array<TextureRegion> temp;

		for(SPRITE_INFO info : SPRITE_INFO.values()) {
			temp = new Array<TextureRegion>(info.FRAME_COUNT);
			for (int x = 0; x < info.FRAME_COUNT; x++) {
				temp.add(tiles[info.INDEX][x]);
			}
			this.graphics.add(temp);
		}
	}

	private static TextureRegion[][] fixBleeding(TextureRegion[][] region) {
		for (TextureRegion[] array : region) {
			for (TextureRegion texture : array) {
				fixBleeding(texture);
			}
		}

		return region;
	}

	private static TextureRegion fixBleeding(TextureRegion region) {
		float fix = 0.01f;

		float x = region.getRegionX();
		float y = region.getRegionY();
		float width = region.getRegionWidth();
		float height = region.getRegionHeight();
		float invTexWidth = 1f / region.getTexture().getWidth();
		float invTexHeight = 1f / region.getTexture().getHeight();
		region.setRegion((x + fix) * invTexWidth, (y + fix) * invTexHeight, (x + width - fix) * invTexWidth, (y + height - fix) * invTexHeight); // Trims
		// region
		return region;
	}
}
