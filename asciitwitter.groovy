/****************************************************************************
 * Copyright (c) Peter Monks 2010, 2011
 * This work is licensed under a Creative Commons Attribution-ShareAlike
 * 3.0 Unported License.  See http://creativecommons.org/licenses/by-sa/3.0/
 * for full details.
 ****************************************************************************/
//@Grab(group="org.apache.commons", module="commons-lang3",  version="3.1")
//@Grab(group="org.slf4j",          module="slf4j-api",      version="1.6.4")
//@Grab(group="ch.qos.logback",     module="logback-core",   version="1.0.1")
@Grab(group="org.twitter4j",      module="twitter4j-core", version="[3.0,)")
@Grab(group='com.github.lalyos', module='jfiglet', version='0.0.3' )

import twitter4j.*
import twitter4j.api.*

import com.github.lalyos.jfiglet.FigletFont

import static org.fusesource.jansi.Ansi.ansi
import org.fusesource.jansi.Ansi.Color
import javax.imageio.ImageIO
 
if (args.length == 0)
{
  println "Please provide your query on the command line.  Note: the hash character (#) needs to be escaped on Unix."
  System.exit(-1)
}
 
print ansi().eraseScreen()

Twitter twitter = TwitterFactory.getSingleton()
Query query = new Query("#groovylang");
QueryResult result;
result = twitter.search(query);
List<Status> statuses = result.getTweets();
//List<Status> statuses = twitter.getHomeTimeline()
for (Status status : statuses) {
	  drawMultiLineStringAt("@${status.getUser().getScreenName()}\n${status.getUser().getName()}",1,34,100)
    def row = 1
  	row += drawMultiLineStringAt(figletMultiLine(wrapAt(status.getUser().getName(),16)),34,row, 100)
    row += drawMultiLineStringAt("@${status.getUser().getScreenName()}",34,row,100) + 2
	  row += drawMultiLineStringAt(status.getText(),34,row,100) + 2
	  row += drawMultiLineStringAt(status.getCreatedAt().toString(),34,row,100)
    drawImage(status.getUser().getMiniProfileImageURL(),1,1,20)
    print ansi().cursor(37,0)
	  sleep(5000)
	  scrollDown(37)
}

void scrollDown(int pRows){
	pRows.times{
		print (ansi().scrollUp(1))
		sleep(50)
	} 
}

String figletMultiLine(String pText){

  StringBuilder sb
  sb = new StringBuilder()

  pText.eachLine(){it, index->
    if (index>0){
      sb.append "\n"
    }
    sb.append(FigletFont.convertOneLine(it))
  }

  return sb.toString();

}

String wrapAt(String pText, int pSize){
  if (!pText||pText.length()<=pSize){
    return pText
  } 
  return pText.split("(?<=\\G.{${pSize}})").join("\n")
}

int drawMultiLineStringAt(String pText, int posX, int posY, int maxRowText){
	def row = 0
	pText.eachLine{it->
     	def line = ''
    	it.split(/\s/).each { word ->
        	if (line.size() + word.size() > maxRowText) {
				print ansi().cursor(posY+row, posX)
				print line
				row++
            	line = ''
        	}
        	line += " $word"
    	}
    	print ansi().cursor(posY+row, posX)
		print line
	    row ++ 
	}
	return row
}
void drawImage(String pUrl, int posX, int posY, int alphaMin) {

def image = ImageIO.read(new URL(pUrl))

def width = 32
def height = 32
def rgb
def hsb
def asciichar
def fgcolor
def bgcolor
def ansivalue
def stepx
def stepy

stepx = image.width/width
stepy = image.height/height

def similarity={java.awt.Color c, java.awt.Color b ->
    return distance = (c.red - b.red)*(c.red - b.red) + (c.green - b.green)*(c.green - b.green) + (c.blue - b.blue)*(c.blue - b.blue)
}

def colors=[
   [ansi:Color.WHITE, rgb:new java.awt.Color(255,255,255)],
   [ansi:Color.BLACK, rgb:new java.awt.Color(0,0,0)],
   [ansi:Color.RED, rgb:new java.awt.Color(255,0,0)],
   [ansi:Color.GREEN, rgb:new java.awt.Color(0,255,0)],
   [ansi:Color.YELLOW, rgb:new java.awt.Color(255,255,0)],
   [ansi:Color.BLUE, rgb:new java.awt.Color(0,0,255)],
   [ansi:Color.MAGENTA, rgb:new java.awt.Color(255,0,255)],
   [ansi:Color.CYAN, rgb:new java.awt.Color(0,255,255)],
]

def similars

for (int y = 0; y < height; y++) {
	def sb = new StringBuilder()
 
	sb.append(ansi().cursor(posY+y,posX))

	for (int x = 0; x < width; x++) {
		rgb = new java.awt.Color(image.getRGB((int)Math.round(stepx*x),(int)Math.round(stepy*y)), true)
        float[] hsbValues = new float[3]
        hsbValues = java.awt.Color.RGBtoHSB(rgb.red,rgb.green,rgb.blue,hsbValues)

        fgcolor = Color.BLACK
        if (rgb.alpha<alphaMin){
        	bgcolor = Color.WHITE
        } else {
	        similars = colors.collect(){
	        	[ ansi:it.ansi,
	        	  similar:similarity(rgb, it.rgb)
	        	]
	        }.sort{it.similar}
	        bgcolor = similars[0].ansi
        }

        if (hsbValues[2]>0.9){
        	asciichar = " "
        } else if (hsbValues[2]>0.8){
        	asciichar = "."
        } else if (hsbValues[2]>0.7){
        	asciichar = ":"
        } else if (hsbValues[2]>0.6){
        	asciichar = ";"
        } else if (hsbValues[2]>0.5){
        	asciichar = "+"
        } else if (hsbValues[2]>0.4){
        	asciichar = "="
        } else if (hsbValues[2]>0.3){
        	asciichar = "░"
        } else if (hsbValues[2]>0.2){
        	asciichar = "▒"
        } else if (hsbValues[2]>0.1){
        	asciichar = "▓"
        } else {
        	asciichar =" "
	        if (rgb.alpha>=alphaMin){
        		fgcolor = bgcolor
        		bgcolor = Color.BLACK
        	}
        }

        ansivalue = ansi().bg(bgcolor).fg(fgcolor).a(asciichar)

		sb.append(ansivalue)
	}
 
	if (!sb.toString().trim()) {
		continue
	}

	sb.append(ansi().reset())
 
	println sb

}

}