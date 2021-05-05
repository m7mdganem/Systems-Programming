from Repository import repo
from DTOs import Vaccine, Supplier, Clinic, Logistic
import sys


def main():
    repo.create_tables()

    # extract all the information from the config file
    extract_from_config(sys.argv[1])

    output = open(sys.argv[3], "w")

    with open(sys.argv[2]) as orders:
        for line in orders:
            order = line.split(",")
            if len(order) == 2:
                repo.send_shipment(*order)
                output.write(repo.get_total_summary())
                output.flush()
            else:
                repo.receive_shipment(*order)
                output.write(repo.get_total_summary())
                output.flush()

    output.close()


def extract_from_config(path):
    with open(path) as config:
        line_number = 0
        for line in config:

            info = line.split(",")

            if line_number == 0:
                vaccines_number = int(info[0])
                suppliers_number = int(info[1])
                clinics_number = int(info[2])
                logistics_number = int(info[3])
                line_number += 1

            elif vaccines_number > 0:
                if repo.max_vacc_id < int(info[0]):
                    repo.max_vacc_id = int(info[0])
                vac = Vaccine(*info)
                repo.vaccines.insert(vac)
                vaccines_number -= 1

            elif suppliers_number > 0:
                supplier = Supplier(*info)
                repo.suppliers.insert(supplier)
                suppliers_number -= 1

            elif clinics_number > 0:
                clinic = Clinic(*info)
                repo.clinics.insert(clinic)
                clinics_number -= 1

            elif logistics_number > 0:
                logistic = Logistic(*info)
                repo.logistics.insert(logistic)
                logistics_number -= 1


if __name__ == '__main__':
    main()
