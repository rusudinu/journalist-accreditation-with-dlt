import { Field, ObjectType } from '@nestjs/graphql';

@ObjectType()
export class ChainUtilsModel {
    @Field()
    result: string;

    constructor(result: string) {
        this.result = result;
    }
}
