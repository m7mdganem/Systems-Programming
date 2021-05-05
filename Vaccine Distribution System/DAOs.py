class _vaccines:
    def __init__(self, conn):
        self._conn = conn

    def insert(self, vaccine):
        self._conn.execute("""INSERT INTO vaccines (id,date,supplier,quantity) VALUES (?,?,?,?) 
                                """, [vaccine.id, vaccine.date, vaccine.supplier, vaccine.quantity])

    def send_shipment(self, amount):
        cursor = self._conn.cursor()
        cursor.execute("""SELECT quantity, id FROM vaccines
                          ORDER BY date ASC""")
        ordered_vaccines = cursor.fetchall()
        for vac in ordered_vaccines:
            vac_quantity = int(vac[0])
            if int(amount) < vac_quantity:
                new_quantity = vac_quantity - int(amount)
                self._conn.execute("UPDATE vaccines SET quantity = ? WHERE id = ? ", [new_quantity, vac[1]])
                break
            else:
                amount = int(amount) - vac_quantity
                self._conn.execute("DELETE FROM vaccines WHERE id = ?", [vac[1]])

    def get_total_quantities(self):
        cursor = self._conn.cursor()
        cursor.execute("SELECT quantity FROM vaccines")
        total = 0
        vaccines = cursor.fetchall()
        for vac in vaccines:
            vac_quantity = int(vac[0])
            total += vac_quantity
        return total


class _suppliers:
    def __init__(self, conn):
        self._conn = conn

    def insert(self, supplier):
        self._conn.execute("""INSERT INTO suppliers (id,name,logistic) VALUES (?,?,?) 
                                """, [supplier.id, supplier.name, supplier.logistic])

    def find_logistic_by_name(self, name):
        cursor = self._conn.cursor()
        cursor.execute("""SELECT logistic FROM suppliers WHERE name = ?
                        """, [name])
        return int(*cursor.fetchone())

    def find_supplier_id(self,name):
        cursor = self._conn.cursor()
        cursor.execute("""SELECT id FROM suppliers WHERE name = ?
                                """, [name])
        return int(*cursor.fetchone())


class _clinics:
    def __init__(self, conn):
        self._conn = conn

    def insert(self, clinic):
        self._conn.execute("""INSERT INTO clinics (id,location,demand,logistic) VALUES (?,?,?,?) 
                                                        """,
                           [clinic.id, clinic.location, clinic.demand, clinic.logistic])

    def send_shipment(self, location, amount):
        cursor = self._conn.cursor()
        cursor.execute("""SELECT demand FROM clinics
                                     WHERE location = ? """, [location])
        new_demand = int(*cursor.fetchone()) - int(amount)
        self._conn.execute("""UPDATE clinics SET demand = ? WHERE location = ?
                                                                """, [new_demand, location])

        cursor.execute("""SELECT logistic FROM clinics
                                     WHERE location = ? """, [location])
        logistic_id = int(*cursor.fetchone())
        cursor.execute("""SELECT count_sent FROM logistics
                              WHERE id = ? """, [logistic_id])
        new_sent = int(*cursor.fetchone()) + int(amount)
        self._conn.execute("""UPDATE logistics SET count_sent = ? WHERE id = ?
                                                        """, [new_sent, logistic_id])

    def get_total_demands(self):
        cursor = self._conn.cursor()
        cursor.execute("SELECT demand FROM clinics")
        demands = cursor.fetchall()
        total = 0
        for demand in demands :
            total += int(demand[0])
        return total



class _logistics:
    def __init__(self, conn):
        self._conn = conn

    def insert(self, logistic):
        self._conn.execute("""INSERT INTO logistics (id,name,count_sent,count_received) VALUES (?,?,?,?) 
                                                """,
                           [logistic.id, logistic.name, logistic.count_sent, logistic.count_received])

    def increase_recieved(self, logistic_id, amount):
        cursor = self._conn.cursor()
        cursor.execute("""SELECT count_received FROM logistics
                              WHERE id = ? """, [logistic_id])
        new_amount = int(amount) + int(*cursor.fetchone())
        self._conn.execute("""UPDATE logistics SET count_received = ? WHERE id = ?
                                                        """, [new_amount, logistic_id])

    def get_total_received(self):
        cursor = self._conn.cursor()
        cursor.execute("SELECT count_received FROM logistics")
        all_received = cursor.fetchall()
        total = 0
        for received in all_received:
            total += int(received[0])
        return total

    def get_total_sent(self):
        cursor = self._conn.cursor()
        cursor.execute("SELECT count_sent FROM logistics")
        all_sent = cursor.fetchall()
        total = 0
        for sent in all_sent:
            total += int(sent[0])
        return total
