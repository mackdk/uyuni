import * as React from "react";
import { useCallback } from "react";

import { MigrationProduct } from "./types";

interface Props {
  className?: string;
  product: MigrationProduct;
  customAddonRenderer?: (id: number, name: string) => JSX.Element;
}

export const MigrationProductList: React.FC<Props> = ({ className, product, customAddonRenderer }): JSX.Element => {
  const renderProduct = useCallback(
    (addon: MigrationProduct): React.ReactNode => {
      if (customAddonRenderer) {
        return customAddonRenderer(addon.id, addon.name);
      }

      return <span>{addon.name}</span>;
    },
    [customAddonRenderer]
  );

  const renderAddons = useCallback(
    (addons: MigrationProduct[]): React.ReactNode => {
      if (addons.length === 0) {
        return <></>;
      }

      return (
        <ul>
          {addons.map((addonProduct) => (
            <li className="addon-product" key={addonProduct.id}>
              {renderProduct(addonProduct)}
              {renderAddons(addonProduct.addons)}
            </li>
          ))}
        </ul>
      );
    },
    [renderProduct]
  );

  return (
    <ul className={`products-list${className ? " " + className : ""}`}>
      <li>
        <strong>{product.name}</strong>
        {renderAddons(product.addons)}
      </li>
    </ul>
  );
};
