import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Project3 extends JFrame implements ActionListener {
    private static int winxpos = 0, winypos = 0;
    private static JFrame myFrame = null;
    private JButton newButton, hitButton, standButton;
    private CardList theDeck = null;
    private CardList playerHand = null;
    private CardList dealerHand = null;
    private JPanel northPanel;
    private MyPanel centerPanel;

    public Project3() {
        myFrame = this;
        northPanel = new JPanel();
        northPanel.setBackground(Color.white);

        newButton = new JButton("New Game");
        newButton.addActionListener(this);
        northPanel.add(newButton);

        hitButton = new JButton("Hit");
        hitButton.addActionListener(this);
        hitButton.setEnabled(false);
        northPanel.add(hitButton);

        standButton = new JButton("Stand");
        standButton.addActionListener(this);
        standButton.setEnabled(false);
        northPanel.add(standButton);

        getContentPane().add("North", northPanel);

        centerPanel = new MyPanel();
        getContentPane().add("Center", centerPanel);

        theDeck = new CardList(52, myFrame);

        setSize(800, 700);
        setLocation(winxpos, winypos);
        setVisible(true);
    }

    public static void main(String[] args) {
        Project3 blackjack = new Project3();
    }

    public static Image load_picture(String fname) {
        Image image;
        MediaTracker tracker = new MediaTracker(myFrame);

        image = myFrame.getToolkit().getImage(fname);
        tracker.addImage(image, 0);
        try {
            tracker.waitForID(0);
        } catch (InterruptedException e) {
            System.err.println(e);
        }

        if (tracker.isErrorID(0)) {
            image = null;
        }
        return image;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == newButton) {
            theDeck = new CardList(52, myFrame);
            playerHand = new CardList(0, myFrame);
            dealerHand = new CardList(0, myFrame);

            dealInitialHands();
            hitButton.setEnabled(true);
            standButton.setEnabled(true);
            repaint();
        } else if (e.getSource() == hitButton) {
            playerHand.insertCard(theDeck.deleteCard(0));
            repaint();
            if (calculateScore(playerHand) > 21) {
                JOptionPane.showMessageDialog(this, "Bust! You lose.");
                hitButton.setEnabled(false);
                standButton.setEnabled(false);
                revealDealerSecondCard();
                repaint();
            }
        } else if (e.getSource() == standButton) {
            dealerHand.insertCard(theDeck.deleteCard(0));
            revealDealerSecondCard();
            repaint();
            dealerTurn();
            determineWinner();
            hitButton.setEnabled(false);
            standButton.setEnabled(false);
            repaint();
        }
    }

    private void dealInitialHands() {
        theDeck.shuffle();
        playerHand.insertCard(new Card(theDeck.deleteCard(0).getCardNumber(), myFrame));
        dealerHand.insertCard(new Card(theDeck.deleteCard(0).getCardNumber(), myFrame));
    }

    private void dealerTurn() {
        int playerScore = calculateScore(playerHand);
        int dealerScore = calculateScore(dealerHand);

        dealerHand.insertCard(theDeck.deleteCard(0));
        repaint();


        while (dealerScore < playerScore && dealerScore <= 21) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            dealerHand.insertCard(theDeck.deleteCard(0));
            dealerScore = calculateScore(dealerHand);
            repaint();
        }
    }

    private void determineWinner() {
        int playerScore = calculateScore(playerHand);
        int dealerScore = calculateScore(dealerHand);

        repaint();

        if (playerScore > 21) {
            JOptionPane.showMessageDialog(this, "Bust! You lose.");
        } else {
            while (dealerScore < playerScore && dealerScore <= 21) {
                Card drawnCard = theDeck.deleteCard(0);
                dealerHand.insertCard(drawnCard);
                dealerScore = calculateScore(dealerHand);
                repaint();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (dealerScore > 21 || playerScore > dealerScore) {
                JOptionPane.showMessageDialog(this, "You win!");
            } else if (playerScore < dealerScore) {
                JOptionPane.showMessageDialog(this, "Dealer wins!");
            } else {
                JOptionPane.showMessageDialog(this, "It's a tie!");
            }
        }
    }

    private void revealDealerSecondCard() {
        Card firstCard = dealerHand.getFirstCard();
        if (firstCard != null && firstCard.getNextCard() != null) {
            ((Card) firstCard.getNext()).setHidden(false);
        }
    }

    private int calculateScore(CardList hand) {
        int score = 0;
        int numAces = 0;

        Card current = hand.getFirstCard();
        while (current != null) {
            int rank = getRank(current);
            if (rank == 1) {
                numAces++;
                score += 11;
            } else if (rank >= 10) {
                score += 10;
            } else {
                score += rank;
            }
            current = current.getNextCard();
        }

        while (score > 21 && numAces > 0) {
            score -= 10;
            numAces--;
        }

        return score;
    }

    private int getRank(Card card) {
        return (card.getCardNumber() % 13) + 1;
    }

    class MyPanel extends JPanel {
        public void paintComponent(Graphics g) {
            int xposPlayer = 25, yposPlayer = 450;
            int xposDealer = 25, yposDealer = 50;

            if (playerHand != null) {
                Card current = playerHand.getFirstCard();
                while (current != null) {
                    Image tempimage = current.getCardImage();
                    g.drawImage(tempimage, xposPlayer, yposPlayer, this);
                    xposPlayer += 80;
                    current = current.getNextCard();
                }
            }

            if (dealerHand != null) {
                Card current = dealerHand.getFirstCard();
                while (current != null) {
                    Image tempimage = current.getCardImage();
                    g.drawImage(tempimage, xposDealer, yposDealer, this);
                    xposDealer += 80;
                    current = current.getNextCard();
                }
            }
        }
    }
}

class Link {
    protected Link next;

    public Link getNext() {
        return next;
    }

    public void setNext(Link newnext) {
        next = newnext;
    }
}

class Card extends Link {
    private Image cardimage;
    private int cardNumber;
    private boolean hidden;
    private JFrame frame;

    public Card(int cardnum, JFrame frame) {
        this.frame = frame;
        cardNumber = cardnum;
        cardimage = load_picture("images/gbCard" + cardnum + ".gif");
        if (cardimage == null) {
            System.out.println("Error - image failed to load: images/gbCard" + cardnum + ".gif");
            System.exit(-1);
        }
        hidden = false;
    }

    public static Image load_picture(String fname) {
        Image image;
        MediaTracker tracker = new MediaTracker(new JPanel());

        image = new JFrame().getToolkit().getImage(fname);
        tracker.addImage(image, 0);
        try {
            tracker.waitForID(0);
        } catch (InterruptedException e) {
            System.err.println(e);
        }

        if (tracker.isErrorID(0)) {
            image = null;
        }
        return image;
    }

    public Card getNextCard() {
        return (Card) next;
    }

    public Image getCardImage() {
        if (hidden) {

            return load_picture("images/gbCard52.gif");
        } else {
            return cardimage;
        }
    }

    public int getCardNumber() {
        return cardNumber;
    }

    public void hide() {
        hidden = true;
    }

    public void setHidden(boolean isHidden) {
        hidden = isHidden;
    }
}


class CardList {
    private Card firstcard = null;
    private int numcards = 0;

    public CardList(int num, JFrame frame) {
        numcards = num;
        for (int i = 0; i < num; i++) {
            Card temp = new Card(i, frame);
            if (firstcard != null) {
                temp.setNext(firstcard);
            }
            firstcard = temp;
        }
    }

    public Card getFirstCard() {
        return firstcard;
    }

    public Card deleteCard(int cardnum) {
        Card target, targetprevious;

        if (cardnum > numcards)
            return null;
        else
            numcards--;

        target = firstcard;
        targetprevious = null;
        while (cardnum-- > 0) {
            targetprevious = target;
            target = target.getNextCard();
            if (target == null)
                return null;
        }
        if (targetprevious != null)
            targetprevious.setNext(target.getNextCard());
        else
            firstcard = target.getNextCard();
        return target;
    }

    public void insertCard(Card target) {
        numcards++;
        if (firstcard != null)
            target.setNext(firstcard);
        else
            target.setNext(null);
        firstcard = target;
    }


    public void shuffle() {
        for (int i = 0; i < 300; i++) {
            int rand = (int) (Math.random() * 100) % numcards;
            Card temp = deleteCard(rand);
            if (temp != null)
                insertCard(temp);
        }
    }
}
