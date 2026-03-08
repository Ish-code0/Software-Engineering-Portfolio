# Software-Engineering-Portfolio by Ismaeel Hussain

Second-year Software Engineering student at Manchester Metropolitan University.

I am currently developing mu skills in:

- Advanced programming (java)
- Full Stack Web Development 
- Problem solving and critical thinking
- Software development processes

This repository is used to keep my projects in one place. Below are some examples of what I have done. 

## Player Performance Tracker (Python)

This program allows users to enter player names and performance scores.
It stores the data, displays a table of players, and allows the user to:

- View the highest performance
- View the lowest performance
- Search for a player
- Save player data to a file
- Load existing player data

```python

firstNumber = int(
    input("Enter the first number: "))  #asks user for the first number
secondNumber = int(
    input("Enter the second number: "))  #asks user for the second number

answer1 = firstNumber + secondNumber  #adds the two numbers together
answer2 = firstNumber - secondNumber  #subtracts the two numbers
answer3 = firstNumber / secondNumber  #divides the two numbers
answer4 = firstNumber * secondNumber  #multiplies the two numbers

print(answer1)  #prints the answer
print(answer2)
print(answer3)
print(answer4)



players = []

for a in range(11):  #11 players
  player_name = input("Enter your player name:")
  player_performance = int(
      input("Enter player performance between 0 and 100:"))
  if player_performance < 0 or player_performance > 100:  #validates the input
    print("Invalid, please try again.")
  players.append(player_name)
  players.append(player_performance)

count = 0
while count < 22:
  print(players[count], ":", (players[count + 1]))
  count = count + 2

# Copy player performance values to results list
results = []
scores = 0
while scores < 22:
  results.append(players[scores + 1])
  scores = scores + 2
  print (results)

# Finds the maximum and minimum values in the results list
result = []
result.append(max(results))
result.append(min(results))
print("The highest and lowest values are:", result)

def find_player(players):
    name = input("Search player: ")
    for x in range(0, len(players), 2): #x is the index I have chosen
        if name == players[x]:
            return f"{name}: {players[x + 1]}" #returns the player name and performance
    return "Player not found."

print(find_player(players))

user_choice = ""  # Initialize choice variable
while user_choice != "h":  # The code will run unless user selects e
  print("Menu:")
  print("a. Display the table of players")
  print("b. View the player with the minimum performance")
  print("c. View the player with the maximum performance")
  print("d. Find a player and display their performance")
  print("e. Do you want to load existing player data?")
  print("f. Do you want to save existing player data? ")
  print("g. Do you want to create new player data? ")
  print("h. Do you want to exit? ")

  user_choice = input(
      "Enter your choice: ")  #allows user to input their choice.

#using the code I created earlier to display the table of player names and performances.
  if user_choice == "a":
    count = 0
    while count < 22:  #22 sets of data- 11 players and 11 performances.
      print(players[count], ":", players[count + 1])
      count = count + 2

  elif user_choice == "b":
        min_performance = min(results)
        min_player = players[results.index(min_performance) * 2]
        print("Player with the minimum performance:", min_player)

  elif user_choice == "c":
        max_performance = max(results)
        max_player = players[results.index(max_performance) * 2]
        print("Player with the maximum performance:", max_player)
  
  elif user_choice == "d":
    print(find_player(players))  #calls the function to find the player and their score.

  elif user_choice == "e":
    filename= "players.txt"

    with open(filename) as f: #opens the file
      contents= f.read()

    print(contents)

  elif user_choice == "f":
    filename= "players.txt"

    with open(filename,'w') as f: #opens the file
      
      f.write(f"Minimum performance: {min(results)}\n")#adds minimum score to file
      f.write(f"Maximum performance: {max(results)}\n")#adds maximum score to file
      f.write(str(players))#writes the players list to file
    print ("file created")

  elif user_choice == "g":
      new_players = []
      for b in range(11):
          player_newname = input("Enter your player name:")
          player_newperformance = int(input("Enter player performance:"))
          new_players.append(player_newname)
          new_players.append(player_newperformance)

      countnew = 0
      while countnew < 22:
          print(new_players[countnew], ":", new_players[countnew + 1])
          countnew = countnew + 2

  elif user_choice == "h":
    print("Exiting the program.")
    break  #exits the program
    
  
else:
   print("Invalid letter. Please try one of the following letters.")
```

## Created a Snake Game using (Python)

This project is a simple Snake game inspired by the classic mobile phone game.

The program was developed using Python and the turtle graphics module.

### Additional info

- Player controls the snake using the keyboard
- Snake grows when food is collected
- Score and high score tracking
- Collision detection with walls and the snake body
- Increasing game speed as the score increases
  
## To run the game you have to

-Install Python 
-Download the relevant files
-Run the program using "python snake_game.py"

### Technologies Used

- Python
- Turtle graphics
- Loops and conditional logic
- Functions

## Created a Basic Website using HTML, CSS and Javascript

This project is a simple, responsive website developed using HTML, CSS, and JavaScript in Visual Studio Code. While the site is functional, I identified areas for improvement. Which includes simplifying the HTML/CSS structure, enhancing the design and UX and adding more secure JavaScript features. In addition, the accessibility could be improved too. Overall, this project was a very valuable learning experience that will help me build more polished and professional websites in the future.



