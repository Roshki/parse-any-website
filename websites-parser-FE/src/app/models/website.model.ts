export class Website {
  public columIndex: number = 1;
  public allPagesHtml: string[] = [];
  public elementsOnMainPage: Map<string, NodeListOf<Element>> = new Map<string, NodeListOf<Element>>();
  public informationToSend: Map<string, string[]> = new Map<string, string[]>();
  public userGuid: string="";

  constructor(init?: Partial<Website>) {
    Object.assign(this, init);
  }
}