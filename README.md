# CWCPokerBot
Poker bot for the Cygni West Coast Virtual Poker Challenge 2015

###Pre-flop
#####To raise:
if 1-p(hand)/constant < Math.random()

#####To call
######Heads up
if p(hand)>0,6*(bet/stack)^(1/8)

######Multi
if p(hand)>0,2*(bet/stack)^(1/8)
