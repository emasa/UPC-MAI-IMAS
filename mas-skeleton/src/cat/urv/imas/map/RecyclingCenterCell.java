/**
 * IMAS base code for the practical work. 
 * Copyright (C) 2014 DEIM - URV
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.map;

import cat.urv.imas.gui.CellVisualizer;
import cat.urv.imas.onthology.GarbageType;

/**
 * Cell that represents a recycling center.
 */
public class RecyclingCenterCell extends Cell {

    /**
     * Prices of recycling plastic, glass and paper, respectively.
     */
    private final int[] prices;
    
    private Integer[] recycledGarbageUnits = new Integer[] {0, 0, 0};
    

    /**
     * Initializes a cell with a hospital.
     *
     * @param row row number (zero based).
     * @param col col number (zero based).
     * @param prices prices of recycling plastic, glass and paper, respectively.
     */
    public RecyclingCenterCell(int row, int col, int[] prices) {
        super(CellType.RECYCLING_CENTER, row, col);
        this.prices = prices;
    }

    /* ***************** Map visualization API ********************************/
    @Override
    public void draw(CellVisualizer visual) {
        visual.drawRecyclingCenter(this);
    }
    
    @Override
    public String getMapMessage() {
        return prices[0] + "/" + prices[1] + "/" + prices[2];
    }
    
    public void recycleGarbage(GarbageType type) {
        if (null != type) switch (type) {
            case PLASTIC:
                ++recycledGarbageUnits[0];
                break;
            case GLASS:
                ++recycledGarbageUnits[1];        
                break;
            case PAPER:
                ++recycledGarbageUnits[2];
                break;
            default:
                break;
        }
    }
        
    public Integer[] getPoints() {
        return new Integer[] {prices[0] * recycledGarbageUnits[0], 
                              prices[1] * recycledGarbageUnits[1], 
                              prices[2] * recycledGarbageUnits[2]};
    }
}
