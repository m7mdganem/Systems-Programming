import sqlite3
from DAOs import _vaccines, _suppliers, _clinics, _logistics
import DTOs
import atexit


class _Repository:
    def __init__(self):
        self._conn = sqlite3.connect('database.db')
        self.vaccines = _vaccines(self._conn)
        self.suppliers = _suppliers(self._conn)
        self.clinics = _clinics(self._conn)
        self.logistics = _logistics(self._conn)
        self.max_vacc_id = 0

    def create_tables(self):
        self._conn.executescript("""
                CREATE TABLE logistics (
                    id              INTEGER     PRIMARY KEY, 
                    name            STRING      NOT NULL, 
                    count_sent      INTEGER     NOT NULL, 
                    count_received  INTEGER     NOT NULL 
                );

                CREATE TABLE clinics (
                    id              INTEGER     PRIMARY KEY, 
                    location        STRING      NOT NULL, 
                    demand          INTEGER     NOT NULL, 
                    logistic        INTEGER     NOT NULL,
                    
                    FOREIGN KEY (logistic) REFERENCES logistics(id)
                );

                CREATE TABLE suppliers (
                    id              INTEGER     PRIMARY KEY, 
                    name            STRING      NOT NULL, 
                    logistic        INTEGER,
                    
                    FOREIGN KEY (logistic) REFERENCES logistics(id)
                );
                
                CREATE TABLE vaccines (
                    id              INTEGER     PRIMARY KEY, 
                    date            DATE        NOT NULL, 
                    supplier        INTEGER,
                    quantity        INTEGER     NOT NULL,
                    
                    FOREIGN KEY (supplier) REFERENCES suppliers(id)
                );
            """)

    def receive_shipment(self, name, amount, date):
        # Get unique id number for the vaccine
        self.max_vacc_id += 1

        # Get the logistics id associated with the supplier with name "name"
        supplier_id = self.suppliers.find_supplier_id(name)

        # Add the vaccine to the vaccines table
        self.vaccines.insert(DTOs.Vaccine(self.max_vacc_id, date, supplier_id, amount))

        # Get the logistics id associated with the supplier with name "name"
        logistic_id = self.suppliers.find_logistic_by_name(name)

        # Increase the received amount in the logistic
        self.logistics.increase_recieved(logistic_id, amount)

    def send_shipment(self, location, amount):
        self.clinics.send_shipment(location, amount)
        self.vaccines.send_shipment(amount)

    def get_total_summary(self):
        total_inventory = self.vaccines.get_total_quantities()
        total_demands = self.clinics.get_total_demands()
        total_received = self.logistics.get_total_received()
        total_sent = self.logistics.get_total_sent()
        return str(total_inventory) + "," + str(total_demands) + "," + str(total_received) + "," + str(
            total_sent) + "\n"

    def _close(self):
        self._conn.commit()
        self._conn.close()


repo = _Repository()
atexit.register(repo._close)
