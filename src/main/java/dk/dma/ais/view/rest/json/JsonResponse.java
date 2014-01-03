/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.ais.view.rest.json;

public abstract class JsonResponse {

    protected int requestId;
    protected int vesselsInWorld;
    
    public JsonResponse(int requestId, int vesselsInWorld) {
        this.requestId = requestId;
        this.vesselsInWorld = vesselsInWorld;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getVesselsInWorld() {
        return vesselsInWorld;
    }

    public void setVesselsInWorld(int vesselsInWorld) {
        this.vesselsInWorld = vesselsInWorld;
    }
    
}
