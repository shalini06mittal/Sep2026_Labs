# Java Lab: Building "ArenaClash" — A Battle Royale Character System

**Theme:** Instead of banking, this lab builds the backend for a battle-royale/RPG game — think Fortnite ranks, Valorant agents, League of Legends champions. Same 10 topics, same incremental build, different (more fun) domain: `ArenaClash`, a system for characters, abilities, loadouts, and a live leaderboard.

**Audience:** Python programmers learning Java syntax/idioms — this assumes you already know how to code, so it focuses on what's *different* in Java.

**Setup:**
```bash
javac *.java
java Main 
```

---

## Stage 1 — Data Types and Operators

```java
public class Main {
    public static void main(String[] args) {
        String gamertag = "ShadowByte";
        int level = 42;
        double health = 100.0;
        double maxHealth = 100.0;
        boolean isAlive = true;
        final int MAX_LEVEL = 100;      // constants -> SCREAMING_SNAKE_CASE by convention
        char rankTier = 'D';            // char = single quotes, single character. Diamond = 'D'

        double healthPercent = health / maxHealth * 100;

        int totalXP = 100;
        int squadSize = 3;

        int avgXP = totalXP / squadSize;  // int / int = int
        System.out.println("Average XP per player: " + avgXP)
        System.out.println(gamertag + " | Lv." + level + " | " + healthPercent + "% HP | Rank: " + rankTier);
        System.out.println("Alive? " + isAlive);
    }
}
```

> **Why?** Java forces you to pick `int` vs `double` up front. `int damage = 10 / 3;` truncates to `3` — a classic bug if you compute crit multipliers or XP splits without casting to `double` first. Get in the habit: `(double) totalXP / squadSize`.

> **Python vs Java:** `char` is single quotes (`'D'`), `String` is double quotes (`"Diamond"`) — Java treats these as genuinely different types, unlike Python where `'a'` and `"a"` are identical.


<details>
<summary>Solution</summary>

```java
        int totalXP = 100;
        int squadSize = 3;

        double avgXP = (double) totalXP / squadSize;  // cast BEFORE dividing
        System.out.println("Average XP per player: " + avgXP);
```
</details>

### Mini-task
Add `int killCount`, `int damageDealt`, compute `double kdRatio = damageDealt / (double) killCount;` — notice the cast, and why skipping it silently gives you `0` for a lot of real KD ratios.

---

## Stage 2 — Conditional Constructs

```java
int level = 42;
double health = 15.0;

if (health <= 0) {
    System.out.println("💀 Eliminated");
} else if (health < 20) {
    System.out.println("⚠️ Critical HP — pop a med kit!");
} else {
    System.out.println("✅ You're good");
}

// Old Style switch - case

char rankTier = 'D';
String rankName;

switch (rankTier) {
    case 'B':
        rankName = "Bronze";
        break;
    case 'S':
        rankName = "Silver";
        break;
    case 'G':
        rankName = "Gold";
        break;
    case 'D':
        rankName = "Diamond";
        break;
    default:
        rankName = "Unranked";
        break;
}

System.out.println("Rank: " + rankName);

// Modern switch (Java 14+) -- great for rank tiers, loot rarity, etc.
char rankTier = 'D';
String rankName = switch (rankTier) {
    case 'B' -> "Bronze";
    case 'S' -> "Silver";
    case 'G' -> "Gold";
    case 'D' -> "Diamond";
    default -> "Unranked";
};
System.out.println("Rank: " + rankName);
```

> **Why no truthy checks?** `if (health)` doesn't compile in Java — conditions must be actual `boolean`s. This kills a whole category of "oops, forgot the comparison" bugs (`if (health != 0)` is required, not optional).

### Mini-task
Classify loot rarity from a drop-chance roll (`int roll` 1–100): 1–50 = Common, 51–80 = Rare, 81–95 = Epic, 96–100 = Legendary. Use if/else, then rewrite using `switch` with ranges via a helper (Java's `switch` doesn't do ranges natively, so bucket the roll into a tier `int` first, then switch on that).

---

## Stage 3 — Loops

```java
// "for" -- use when you know the number of iterations up front
int rounds = 5;
double totalDamage = 0;

for (int round = 1; round <= rounds; round++) {
    double roundDamage = round * 45.5;   // stand-in formula for "damage this round"
    totalDamage += roundDamage;
    System.out.println("Round " + round + ": " + roundDamage + " dmg");
}
System.out.println("Total damage after " + rounds + " rounds: " + totalDamage);

// "while" -- use when you DON'T know the iteration count ahead of time;
// here, how many hits it takes to reach 0 HP depends on the damage per hit
double health = 100.0;
int hitsTaken = 0;
while (health > 0) {
    health -= 30;
    hitsTaken++;
    System.out.println("Took a hit! HP now: " + Math.max(health, 0));
}
System.out.println("Eliminated after " + hitsTaken + " hits.");

// "do-while" -- guarantees at least one execution: you always respawn once before checking lives
int lives = 3;
do {
    System.out.println("Respawning... lives left: " + lives);
    lives--;
} while (lives > 0);
```

> **for vs while: both can express the same loop, but pick the one that documents your intent. A fixed number of rounds → for. "Keep going until some condition becomes false, and I don't know how many steps that'll take" → while. Reaching for the right one makes the code self-explanatory.

### Mini-task
Simulate a 6-round match with a for loop where round n's damage is n * 20 + 10 (a made-up formula, no array needed). Track: the total damage, the single highest-damage round, and how many rounds dealt over 100 damage — using only loop variables.

---

## Stage 4 — Arrays Basics

Now that we have real *collections* of data (5 match results, not just a formula), arrays — and the for-each loop that goes with them — finally make sense.

```java
double[] matchDamage = {245.5, 89.0, 512.3, 0.0, 178.0}; // damage per match, last 5 games

double totalDamage = 0;
for (double dmg : matchDamage) {     // for-each: read-only pass over VALUES -- no index needed
    totalDamage += dmg;
}
System.out.println("Total damage (5 games): " + totalDamage);

for (int i = 0; i < matchDamage.length; i++) {   // indexed: use when the POSITION itself matters
    System.out.println("Game " + (i + 1) + ": " + matchDamage[i] + " dmg");
}
```

> **Why two ways to loop over the same array?** For-each (`for (double d : arr)`) is what you reach for 90% of the time — it's safer (can't miscount an index) and reads cleanly. Use the indexed version only when you need the position too, like printing "Game 1, Game 2, ..." labels.



```java
String[] loadout = new String[4];   // 4 weapon slots, fixed size
loadout[0] = "Plasma Rifle";
loadout[1] = "Shotgun";
loadout[2] = "Med Kit";
loadout[3] = "Smoke Grenade";

System.out.println("Slots: " + loadout.length);  // .length -- field, no parentheses

int[][] squadDamageByRound = {     // 2D array: rows = squad members, columns = rounds
    {120, 340, 210},   // player 1
    {90, 150, 400},    // player 2
    {0, 220, 180}       // player 3
};
System.out.println("Player 2, round 3: " + squadDamageByRound[1][2]);
```

> **Why fixed-size arrays here?** A loadout genuinely has a fixed number of slots (4 weapons, always 4) — this is exactly the case arrays are built for. When your collection's size is naturally variable (an inventory that grows/shrinks), that's what `ArrayList` (Stage 9) is for — don't force a fixed array there.

### Mini-task
Given `matchDamage` above, find the highest single-game damage, the average, and count how many games had zero damage (a "died before dealing damage" stat) — using a for-each loop. Then store 5 squadmates' health values in an `int[]` and write loop logic that returns the "squad average HP" and flags if any teammate is under 20 HP ("needs a revive").

---

## Stage 5 — Methods

```java
public class Main {

    static double calculateDamage(double baseDamage, double critMultiplier) {
        return baseDamage * critMultiplier;
    }

    // overloading: no crit specified = assume no crit (1.0x)
    static double calculateDamage(double baseDamage) {
        return calculateDamage(baseDamage, 1.0);
    }

    static String getRankName(char tier) {
        return switch (tier) {
            case 'B' -> "Bronze";
            case 'S' -> "Silver";
            case 'G' -> "Gold";
            case 'D' -> "Diamond";
            default -> "Unranked";
        };
    }

    public static void main(String[] args) {
        System.out.println(calculateDamage(50, 2.0));  // headshot crit
        System.out.println(calculateDamage(50));        // normal hit
        System.out.println(getRankName('G'));
    }
}
```

> **Why overloading, not default params?** Java resolves which method to call at *compile time* based on argument types/count — there's no `def calculate_damage(base, crit=1.0):` shortcut. Overloading is the Java-native way to express "here are a few valid ways to call this."

### Mini-task
Extract Stage 3/4's logic into methods: `totalDamage(double[] rounds)`, `averageHealth(int[] squadHealth)`, `needsRevive(int health)` returning `boolean`.

---

## Stage 6 — Classes, Objects, Constructors, Getters/Setters, toString, Encapsulation

```java
public class Character {
    private String name;
    private int level;
    private double health;
    private double maxHealth;
    private final String characterClass; // set once at creation, e.g. "Mage"

    public Character(String name, int level, double maxHealth, String characterClass) {
        this.name = name;
        this.level = level;
        this.maxHealth = maxHealth;
        this.health = maxHealth;          // start at full HP
        this.characterClass = characterClass;
    }

    public double getHealth() {
        return health;
    }

    public String getName() {
        return name;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void takeDamage(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage can't be negative");
        }
        health -= amount;
        if (health < 0) health = 0;    // can't go below 0 HP
    }

    public void heal(double amount) {
        health = Math.min(health + amount, maxHealth);  // can't overheal past max
    }

    @Override
    public String toString() {
        return name + " (Lv." + level + " " + characterClass + ") — " + health + "/" + maxHealth + " HP";
    }
}
```

> **Why is `health` private?** If `health` were a public field, anywhere in your codebase someone could write `character.health = 99999` or `character.health = -50`, bypassing your max-HP cap and death rules entirely. Making it private and only mutable via `takeDamage()`/`heal()` means your class *guarantees* HP always stays between `0` and `maxHealth` — no matter who calls it or how. This is encapsulation: the object protects its own rules.

> **Why no public setter for `health`?** Notice there's no `setHealth()`. Not every field needs a setter — only expose the mutations that make sense for your game's rules (damage, heal), not arbitrary overwrites.

### Mini-task
Create `Character.java`. In `Main`, create 3 characters, deal damage and heal them, print each via `toString()`. Add an `equals()` that compares by `name` (like Python's `__eq__`).

---

## Stage 7 — Inheritance and Polymorphism

```java
public class Mage extends Character {
    private double mana;

    public Mage(String name, int level, double maxHealth, double mana) {
        super(name, level, maxHealth, "Mage");   // must call parent constructor first
        this.mana = mana;
    }

    public void castFireball() {
        if (mana < 20) {
            System.out.println(getName() + " is out of mana!");
            return;
        }
        mana -= 20;
        System.out.println(getName() + " casts Fireball! 🔥");
    }

    @Override
    public String toString() {
        return super.toString() + " | Mana: " + mana;
    }
}

public class Warrior extends Character {
    private double armor;

    public Warrior(String name, int level, double maxHealth, double armor) {
        super(name, level, maxHealth, "Warrior");
        this.armor = armor;
    }

    @Override
    public void takeDamage(double amount) {
        double reduced = amount * (1 - armor);   // armor reduces incoming damage
        super.takeDamage(reduced);                // reuse parent logic for the actual HP math
    }
}
```

Polymorphism — different character types, one loop:
```java
Character[] squad = {
    new Mage("ShadowByte", 42, 100, 80),
    new Warrior("TankZilla", 38, 150, 0.3)
};

for (Character c : squad) {
    c.takeDamage(50);   // Warrior's overridden version runs for TankZilla, base version for ShadowByte
    System.out.println(c);
}
```

> **Why does this matter?** In Python, duck typing means any object with `.take_damage()` works, no relation required. Java requires `Mage` and `Warrior` to formally `extend Character` — but once they do, this loop treats them uniformly *while still running each one's own overridden behavior* (Warrior's damage reduction, Mage's own `toString`). This is **dynamic dispatch**: the actual object's type decides which method body runs, decided at runtime, not by the array's declared type.

### Mini-task
Add a `Healer` subclass with a `healAlly(Character ally, double amount)` method. Build a squad array of mixed types, run a "round" where everyone takes 30 damage, then have the Healer heal the lowest-HP teammate.

---

## Stage 8 — Interfaces

```java
public interface Ultimate {
    void activateUltimate();

    default String ultimateReadyMessage() {   // default method: shared logic across implementers
        return "Ultimate ready! Press Q!";
    }
}

public class Mage extends Character implements Ultimate {
    // ...existing Mage code...

    @Override
    public void activateUltimate() {
        System.out.println(getName() + " unleashes METEOR STORM! ☄️☄️☄️");
    }
}
```

> **Why an interface instead of just more inheritance?** Not every character has an ultimate ability (maybe a basic NPC doesn't), and `Character` can only `extend` one parent. `implements Ultimate` lets you say "this character ALSO has an ultimate" without forcing every single `Character` subclass to carry ultimate logic it doesn't need. A `Warrior` and a `Mage` can both implement `Ultimate` even though they're unrelated in every other way — interfaces group by *capability*, not by ancestry.

### Mini-task
Define `Ultimate`. Make `Mage` and `Warrior` implement it. Write `triggerAllUltimates(Character[] squad)` that loops through, uses `instanceof` to check `if (c instanceof Ultimate u)` (Java's pattern-matching `instanceof`), and calls `u.activateUltimate()` only on those that have it.

---

## Stage 9 — ArrayList and Map

```java
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

List<String> inventory = new ArrayList<>();   // dynamic, unlike a fixed loadout array
inventory.add("Med Kit");
inventory.add("Smoke Grenade");
inventory.add("Med Kit");           // duplicates allowed, like a Python list
inventory.remove("Smoke Grenade");  // easy removal -- arrays can't do this natively
System.out.println(inventory);      // [Med Kit, Med Kit]

// Leaderboard: gamertag -> total XP, like a Python dict
Map<String, Integer> leaderboard = new HashMap<>();
leaderboard.put("ShadowByte", 15200);
leaderboard.put("TankZilla", 9800);
leaderboard.put("HealQueen", 12100);

leaderboard.put("ShadowByte", leaderboard.get("ShadowByte") + 500);  // add XP after a match

for (Map.Entry<String, Integer> entry : leaderboard.entrySet()) {
    System.out.println(entry.getKey() + ": " + entry.getValue() + " XP");
}
```

> **Why `List<String>` as the declared type (not `ArrayList<String>`)?** Same reason as before: code against the interface. If you later swap to a `LinkedList` for performance reasons, nothing that *uses* `inventory` needs to change.

> **Why generics (`<String, Integer>`)?** Without them, `leaderboard.get("ShadowByte")` would return a generic `Object`, and you'd have to cast it to `Integer` every single time, risking a runtime crash if the wrong type ever sneaks in. Generics push that type-check to compile time — the compiler literally won't let you `leaderboard.put("Bob", "not a number")`.

### Mini-task
Build a `Squad` class with a `List<Character> members` and a `Map<String, Integer> xpByPlayer`. Add `addMember(Character c)`, `totalSquadHealth()` (loop + sum), and `topPlayer()` (loop over the map to find the max XP entry — like Python's `max(dict, key=dict.get)`, but written out manually since Java's collections don't have that one-liner).

---

## Stage 10 — Exception Handling

```java
public class OutOfManaException extends Exception {   // checked exception
    public OutOfManaException(String message) {
        super(message);
    }
}

public class Mage extends Character implements Ultimate {
    private double mana;
    // ...

    public void castFireball() throws OutOfManaException {
        if (mana < 20) {
            throw new OutOfManaException(getName() + " doesn't have enough mana!");
        }
        mana -= 20;
        System.out.println(getName() + " casts Fireball! 🔥");
    }
}
```

Calling code must handle it:
```java
Mage mage = new Mage("ShadowByte", 42, 100, 15);  // only 15 mana

try {
    mage.castFireball();
} catch (OutOfManaException e) {
    System.out.println("Spell failed: " + e.getMessage());
} finally {
    System.out.println("Turn ended.");   // always runs, win or lose
}
```

> **Why a *checked* exception here?** Running out of mana during a match is a completely normal, expected game event (not a bug) — checked exceptions force every caller of `castFireball()` to explicitly plan for the "no mana" case, the same way real game code has to show a UI message or trigger a cooldown animation instead of just crashing. Compare to `IllegalArgumentException` in Stage 6 (unchecked) — that represents a programming mistake (passing negative damage), which you fix in code, not "handle" at runtime.

### Mini-task (final integration)
1. Create `OutOfManaException` and a `TargetEliminatedException` (checked) thrown when you try to damage a character whose `isAlive()` is already `false`.
2. Update `Character.takeDamage()` to throw `TargetEliminatedException` if the target is already dead.
3. In `Squad`, add `attack(Character attacker, Character target, double damage)` that catches both exceptions, prints a friendly battle-log message on failure, and lets the match continue either way (no crash).
4. Print a final **match summary**: surviving squad members, the leaderboard sorted by XP, and each `Ultimate`-capable character's ultimate status.

You now have a full `ArenaClash` backend touching typed stats and damage math (1), rank/loot logic (2), per-match loops (3), fixed loadouts and squad arrays (4), reusable combat methods (5), an encapsulated `Character` class (6), a `Mage`/`Warrior`/`Healer` hierarchy with polymorphic combat (7), an `Ultimate` interface (8), a dynamic `Squad` roster and XP leaderboard via `List`/`Map` (9), and crash-proof combat via checked exceptions (10).

---

## Where to go next
- **Records** for lightweight immutable data like `record LootDrop(String itemName, String rarity) {}`.
- **Streams** to rewrite `topPlayer()` as `xpByPlayer.entrySet().stream().max(Map.Entry.comparingByValue())` once comprehensions-as-loops feel natural.
- **Enums** (`enum Rarity { COMMON, RARE, EPIC, LEGENDARY }`) instead of raw chars/strings for rank tiers and loot rarity — much safer than string comparisons.
- Multiplayer-style concurrency (`Thread`/`ExecutorService`) if you want to simulate simultaneous player actions.
